package com.example.pitapp.ui.features.classrooms.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pitapp.R

@Composable
fun ClassroomDialog(
    title: String,
    titleIcon: ImageVector,
    initialNumber: String,
    initialDescription: String,
    errorMessage: String?,
    isEditMode: Boolean,
    onConfirm: (number: Int, description: String) -> Unit,
    onDismiss: () -> Unit
) {
    var numberText by remember { mutableStateOf(initialNumber) }
    var descriptionText by remember { mutableStateOf(initialDescription) }
    var numberError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    fun validateAndConfirm() {
        val number = numberText.toIntOrNull()
        numberError = if (number == null && numberText.isNotBlank()) {
            context.getString(R.string.validation_must_be_integer)
        } else if (!isEditMode && numberText.isBlank()) {
            context.getString(R.string.validation_number_required)
        } else {
            null
        }
        if (numberError == null && (isEditMode || number != null)) {
            onConfirm(number ?: initialNumber.toInt(), descriptionText)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    titleIcon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = numberText,
                    onValueChange = { text ->
                        if (text.all { it.isDigit() } || text.isEmpty()) {
                            numberText = text
                            if (numberError != null && (text.toIntOrNull() != null || text.isBlank())) {
                                numberError = null
                            }
                        }
                    },
                    label = { Text(text = stringResource(R.string.label_number)) },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.ConfirmationNumber,
                            contentDescription = null
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = numberError != null,
                    enabled = !isEditMode,
                    supportingText = {
                        if (numberError != null) Text(numberError!!)
                    }
                )
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text(text = stringResource(R.string.label_description)) },
                    leadingIcon = { Icon(Icons.Outlined.Description, contentDescription = null) },
                    singleLine = false,
                    maxLines = 3,
                    isError = errorMessage != null && errorMessage.contains(
                        stringResource(R.string.label_description),
                        ignoreCase = true
                    ),
                )
                if (errorMessage != null && numberError == null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { validateAndConfirm() },
                enabled = numberError == null && (isEditMode || numberText.isNotBlank())
            ) {
                Icon(
                    Icons.Filled.CheckCircleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = stringResource(R.string.save_changes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Icon(
                    Icons.Filled.Cancel,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(text = stringResource(R.string.cancel))
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}






