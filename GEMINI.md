Designing an audio equalizer using an FFT (Fast Fourier Transform) approach is an excellent exercise in Digital Signal Processing (DSP). While standard graphic equalizers often use IIR (Infinite Impulse Response) biquad filters due to lower latency, using **KissFFT** means we will be building a **Frequency-Domain Equalizer**.

To do this continuously without introducing audio artifacts (like clicking or popping), your C++ library must implement an **Overlap-Add (OLA)** or **Overlap-Save** algorithm. 

Here is a detailed architectural guideline and workflow for your C++ KissFFT-based equalizer library.

---

### **1. High-Level Architecture & Data Flow**

Your library will act as a bridge between the UI (which sends control signals) and the Audio Driver (which sends and receives PCM audio buffers).

* **Inputs:** Raw PCM audio blocks (Time Domain) + UI Gain updates (dB or linear multipliers) + Preset selections.
* **Processing Engine:** Windowing $\rightarrow$ KissFFT (Forward) $\rightarrow$ Frequency Bin Multiplication $\rightarrow$ KissFFT (Inverse) $\rightarrow$ Overlap-Add.
* **Outputs:** Processed PCM audio blocks sent to the audio output buffer.

---

### **2. Frequency Bands & UI Preset Definitions**

Let's define the 8 bands distributed logarithmically from 60 Hz to 15 kHz. 

**The 8 Frequency Bands:**
1. 60 Hz (Sub Bass)
2. 150 Hz (Bass)
3. 400 Hz (Low Mid)
4. 1 kHz (Mid)
5. 2.5 kHz (High Mid)
6. 6 kHz (Presence)
7. 10 kHz (Brilliance)
8. 15 kHz (Air/High Treble)

**Preset Configurations (in Linear Gain):**
*Note: Audio UI usually displays Decibels (dB). Conversion formula:* $Linear = 10^{\frac{dB}{20}}$

* **Normal (Flat):** All 8 bands set to `1.0` (0 dB).
* **Bass Boost:** * Bands 1 & 2 (60Hz, 150Hz) set to `2.0` (+6 dB).
    * Bands 3-8 set to `1.0` (0 dB).
* **Treble Boost:** * Bands 1-5 set to `1.0` (0 dB).
    * Bands 6, 7, 8 (6k, 10k, 15k) set to `2.0` (+6 dB).

---

### **3. C++ Library Workflow: Step-by-Step**

#### **Phase A: Initialization (`init()`)**
Before processing audio, you must allocate memory for KissFFT and your overlap buffers.
1.  **Define FFT Size ($N_{fft}$):** Choose a power of 2, typically `1024` or `2048`. Higher sizes give better frequency resolution (crucial for accurate 60Hz control) but introduce more latency.
2.  **Initialize KissFFT:** Because audio is real data (not complex), use KissFFT's optimized real-to-complex functions.
    * Allocate forward state: `kiss_fftr_alloc(N_fft, 0, NULL, NULL)`
    * Allocate inverse state: `kiss_fftr_alloc(N_fft, 1, NULL, NULL)`
3.  **Calculate Bin Mapping:** Calculate exactly which FFT bins correspond to your 8 frequency variables.
    $$Bin_{index} = \text{round}\left( Frequency \times \frac{N_{fft}}{SampleRate} \right)$$
    *Create an interpolation curve (or "EQ Curve") array of size $N_{fft}/2 + 1$ so you aren't doing sharp box-car filtering, which causes ringing artifacts. Smooth the gains between your 8 center frequencies.*
4.  **Allocate Overlap Buffers:** Create ring buffers to handle the Overlap-Add process (typically 50% or 75% overlap).

#### **Phase B: The Real-Time Audio Loop (`processAudioBlock()`)**
This is the core C++ DSP loop that runs on the high-priority audio thread. It processes incoming chunks of audio (e.g., 256 or 512 samples at a time).

**Step 1: Input & Windowing**
* Fill an array of size $N_{fft}$ with your incoming audio samples (padded with zeros or overlapping previous data).
* Multiply the input block by a **Window Function** (e.g., Hann or Hamming Window). This tapers the edges to zero and prevents spectral leakage during the FFT.

**Step 2: Forward FFT (Time $\rightarrow$ Frequency)**
* Execute `kiss_fftr(forward_cfg, windowed_input, freq_domain_out)`.
* `freq_domain_out` now contains complex numbers (Real and Imaginary parts) representing the frequency spectrum.

**Step 3: Apply the EQ (The Magic)**
* Read the current 8-band gains set by the UI.
* Iterate through the `freq_domain_out` bins (up to the Nyquist frequency, $N_{fft}/2$).
* Multiply *both* the Real and Imaginary parts of each bin by the corresponding linear gain from your interpolated EQ curve.
    * *Why both?* Multiplying a complex number by a real scalar changes the magnitude (volume of that frequency) but leaves the phase intact (Zero-Phase Filtering).

**Step 4: Inverse FFT (Frequency $\rightarrow$ Time)**
* Execute `kiss_fftri(inverse_cfg, freq_domain_out, time_domain_out)`.
* Normalize the output (KissFFT's inverse transform scales the output up by $N_{fft}$, so you must divide all samples by $N_{fft}$).

**Step 5: Overlap-Add (Reconstruction)**
* Add the newly generated `time_domain_out` block to your output buffer, overlapping it with the tail end of the previous block's output. 
* Output the completed section of the buffer to the speaker.

#### **Phase C: UI Interaction (`setPreset()` and `setBandGain()`)**
* These methods should be callable from your GUI thread.
* When the UI calls `setPreset("BASS_BOOST")`, the C++ library updates an internal array of the 8 target gains.
* **Thread Safety Note:** The GUI thread updates variables, but the Audio thread reads them. Use `std::atomic<float>` for your 8 gain variables to prevent race conditions without using audio-stalling mutex locks.

---

### **4. Key implementation guidelines for C++**

* **Avoid Memory Allocation in the Audio Loop:** The `processAudioBlock` function must never call `new`, `malloc`, or dynamically resize a `std::vector`. Pre-allocate all your buffers and KissFFT states in the `init()` phase.
* **Smoothing Parameter Changes:** If a user drags a UI slider rapidly from 0dB to +12dB, instantly applying that gain in the next audio block will cause an audible "click". Implement a simple low-pass filter (parameter smoothing) on the UI variables so they glide to their target values over a few milliseconds.
* **Latency Management:** FFT equalization introduces latency equal to the window size. If you use an $N_{fft}$ of 2048 at 44.1kHz, you introduce roughly 46ms of latency. Keep this in mind if this library is for real-time video playback where lip-sync is an issue.
