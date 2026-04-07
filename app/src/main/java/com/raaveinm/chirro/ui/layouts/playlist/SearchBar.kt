package com.raaveinm.chirro.ui.layouts.playlist

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.raaveinm.chirro.ui.veiwmodel.PlayerViewModel
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
@Composable
fun SearchBar(
    viewModel: PlayerViewModel = viewModel(),
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
    closeScreen: () -> Unit
) {
    val uiState by viewModel.searchUiState.collectAsState()
    val searchQuery = uiState.currentText ?: ""

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { closeScreen() },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "back"
            )
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onTextChanged(it) },
            modifier = Modifier.weight(6f),
            trailingIcon = {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                    )
                } else if (!uiState.currentText.isNullOrEmpty()) {
                    IconButton(onClick = { viewModel.clearSearch() }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            isError = uiState.searchResults.isEmpty() && searchQuery.isNotEmpty(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            singleLine = true,
            maxLines = 1,
        )

        IconButton(
            onClick = { viewModel.onSearchToggle() },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "search"
            )
        }
    }
}
