```java
         //for assets folder

        try {
            AssetFileDescriptor fld = getAssets().openFd("faint.mp3");
            player.setDataSource(fld.getFileDescriptor(), fld.getStartOffset(), fld.getLength());
            player.prepare();
        }catch (IOException e){
            Log.e("PLAYER-ERR","there "+ e.getMessage());
        }

        // second variant

        String fld = "faint.mp3";
        Uri uri = Uri.parse(fld);

        try {
            player.setDataSource(String.valueOf(uri));
            player.prepareAsync();
        } catch (IOException e) {
            Log.e("PLAYER-ERR","there "+ e.getMessage());
        }
        */

        // res/raw

        MediaPlayer player = MediaPlayer.create(this, R.raw.faint);
        player.start();
        
        try {
            player.setDataSource(getResources().openRawResourceFd(R.raw.faint));
            player.prepareAsync();
        } catch (IOException e) {
            Log.e("PLAYER-ERR", "there " + e.getMessage());
        }
```