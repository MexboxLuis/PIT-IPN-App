package com.example.pitapp.ui.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.pitapp.ui.model.MenuItem

@Composable
fun GenericDropDownMenu(
    menuItems: List<MenuItem>,
    expanded: Boolean,
    onDismissRequest: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onDismissRequest() }
    ) {
        menuItems.forEach { item ->
            DropdownMenuItem(
                text = { Text(text = item.text) },
                onClick = {
                    item.action()
                    onDismissRequest()
                },
                leadingIcon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null
                    )
                }
            )
        }
    }
}