package com.example.pitapp.ui.features.classes.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pitapp.R

@Composable
fun AddStudentForm(
    name: String,
    studentId: String,
    academicProgram: String,
    email: String,
    isRegular: Boolean,
    onEmailChange: (String) -> Unit,
    onIsRegularChange: (Boolean) -> Unit,
    onAddStudent: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = {},
            label = { Text(text = stringResource(R.string.full_name)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = studentId,
            onValueChange = {},
            label = { Text(stringResource(R.string.student_id)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Badge,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = academicProgram,
            onValueChange = {},
            label = { Text(text = stringResource(R.string.academic_program)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text(text = stringResource(R.string.email)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = MaterialTheme.shapes.small,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            onClick = {
                onIsRegularChange(!isRegular)
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isRegular) Icons.Default.PersonAddAlt1 else Icons.Default.PersonAddAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (isRegular) stringResource(R.string.student_regular) else stringResource(
                            R.string.student_irregular
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Switch(
                    checked = isRegular,
                    onCheckedChange = onIsRegularChange
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = onAddStudent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(text = stringResource(R.string.add_student))
        }
    }
}