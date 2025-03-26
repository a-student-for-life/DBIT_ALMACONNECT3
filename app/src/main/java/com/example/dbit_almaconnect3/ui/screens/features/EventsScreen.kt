package com.example.dbit_almaconnect3.ui.screens.features

// Use an alias for android.graphics.Color to avoid conflicts with Compose's Color
import android.graphics.Color as AndroidColor
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.dbit_almaconnect3.data.repository.FlarumTagRepository
import com.example.dbit_almaconnect3.data.repository.Tag
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import kotlinx.coroutines.launch

// --------------------- HELPER EXTENSION ---------------------
// Extension to convert Compose Color to a hex string.
fun Color.toHexString(): String {
    return String.format("#%06X", 0xFFFFFF and this.toArgb())
}

// --------------------- CUSTOM COLOR SWATCH ---------------------
@Composable
fun CustomColorSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .clickable { onClick() }
            .border(
                width = if (selected) 4.dp else 2.dp,
                color = if (selected) Color.Black else Color.Gray,
                shape = CircleShape
            )
    )
}

// --------------------- CUSTOM COLOR PICKER ROWS ---------------------
@Composable
fun CustomColorPickerRows(
    primaryColors: List<Color>,
    additionalShades: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    Column {
        Text(text = "Primary Colors", fontSize = 14.sp)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(primaryColors) { color ->
                CustomColorSwatch(
                    color = color,
                    selected = (color == selectedColor),
                    onClick = { onColorSelected(color) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Additional Shades", fontSize = 14.sp)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(additionalShades) { color ->
                CustomColorSwatch(
                    color = color,
                    selected = (color == selectedColor),
                    onClick = { onColorSelected(color) }
                )
            }
        }
    }
}

// --------------------- TAG CARD (formerly TagItem) ---------------------
@Composable
fun TagCard(
    tag: Tag,
    onClick: () -> Unit
) {
    // Use a default color if tag.color is null or empty.
    val colorString = if (tag.color.isNullOrEmpty()) "#D3D3D3" else tag.color
    val backgroundColor = try {
        Color(AndroidColor.parseColor(colorString))
    } catch (e: Exception) {
        Color.LightGray
    }
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tag.name,
                fontSize = 18.sp,
                color = Color.White
            )
        }
    }
}

// --------------------- ALUMNI EVENTS / REUNIONS SCREEN ---------------------
@Composable
fun AlumniEventsScreen(email: String, navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val flarumTagRepository = remember { FlarumTagRepository() }

    var graduationYear by remember { mutableStateOf("") }
    var discussionTitle by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFFFFFFFF)) }
    var tagList by remember { mutableStateOf<List<Tag>>(emptyList()) }

    val primaryColors = listOf(
        Color(0xFFFF0000), // Red
        Color(0xFFFFA500), // Orange
        Color(0xFFFFFF00), // Yellow
        Color(0xFF008000), // Green
        Color(0xFF0000FF), // Blue
        Color(0xFF800080)  // Purple
    )
    val additionalShades = listOf(
        Color(0xFFFFC0CB), // Pink
        Color(0xFF808080), // Gray
        Color(0xFF000000), // Black
        Color(0xFFFFFFFF), // White
        Color(0xFFB22222), // Firebrick
        Color(0xFF8B4513), // SaddleBrown
        Color(0xFF2E8B57), // SeaGreen
        Color(0xFF4682B4), // SteelBlue
        Color(0xFFDAA520)  // Goldenrod
    )

    // Load existing alumni reunion discussions (tags) under "alumni-reunions".
    LaunchedEffect(Unit) {
        val allTags = flarumTagRepository.getTags() ?: return@LaunchedEffect
        val alumniParentId = allTags.firstOrNull { it.slug == "alumni-reunions" }?.id
        if (alumniParentId != null) {
            tagList = allTags.filter { it.parentId == alumniParentId }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Title at the top.
        Text(text = "Alumni Reunion Planning for $email", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // List of discussions fills the available space.
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(tagList) { tag ->
                TagCard(tag = tag) {
                    val tagUrl = "http://129.154.249.30:8080/t/${tag.slug}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tagUrl))
                    context.startActivity(intent)
                }
            }
        }

        // The creation UI is pinned at the bottom.
        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = graduationYear,
                onValueChange = { graduationYear = it },
                label = { Text("Enter Graduation Year (e.g., 2015)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = discussionTitle,
                onValueChange = { discussionTitle = it },
                label = { Text("Discussion Title (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Select Tag Color:", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            CustomColorPickerRows(
                primaryColors = primaryColors,
                additionalShades = additionalShades,
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        val allTags = flarumTagRepository.getTags()
                        val parentId = allTags?.firstOrNull { it.slug == "alumni-reunions" }?.id
                        if (parentId == null) {
                            Toast.makeText(context, "Alumni parent tag missing. Create it first.", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        // If graduationYear is provided, use it to create the title with "Alumni Reunion" prefixed.
                        // Otherwise, use the discussionTitle.
                        val finalTitle = if (graduationYear.isNotBlank()) {
                            "Alumni Reunion $graduationYear"
                        } else if (discussionTitle.isNotBlank()) {
                            discussionTitle
                        } else {
                            ""
                        }
                        if (finalTitle.isBlank()) {
                            Toast.makeText(context, "Please enter a graduation year or a discussion title.", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                        val newTag = flarumTagRepository.createTag(
                            name = finalTitle,
                            color = selectedColor.toHexString(),
                            parentTagId = parentId
                        )
                        if (newTag != null) {
                            Toast.makeText(context, "Discussion '${newTag.name}' created", Toast.LENGTH_SHORT).show()
                            // Refresh the list.
                            flarumTagRepository.getTags()?.let { updatedTags ->
                                tagList = updatedTags.filter { it.parentId == parentId }
                            }
                            graduationYear = ""
                            discussionTitle = ""
                        } else {
                            Toast.makeText(context, "Failed to create discussion", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Reunion Discussion")
            }
        }
    }
}


@Composable
fun StudentEventsScreen(email: String, navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val flarumTagRepository = remember { FlarumTagRepository() }

    // Dropdown state variables
    val yearOptions = listOf("FE", "SE", "TE", "BE")
    var expanded by remember { mutableStateOf(false) }
    var selectedYear by remember { mutableStateOf(yearOptions.first()) }

    // Event name input field.
    var eventName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFFFFFFFF)) }
    var tagList by remember { mutableStateOf<List<Tag>>(emptyList()) }

    val primaryColors = listOf(
        Color(0xFFFF0000),
        Color(0xFFFFA500),
        Color(0xFFFFFF00),
        Color(0xFF008000),
        Color(0xFF0000FF),
        Color(0xFF800080)
    )
    val additionalShades = listOf(
        Color(0xFFFFC0CB),
        Color(0xFF808080),
        Color(0xFF000000),
        Color(0xFFFFFFFF),
        Color(0xFFB22222),
        Color(0xFF8B4513),
        Color(0xFF2E8B57),
        Color(0xFF4682B4),
        Color(0xFFDAA520)
    )

    // Load existing student event discussions (tags) under "student-events".
    LaunchedEffect(Unit) {
        val allTags = flarumTagRepository.getTags() ?: return@LaunchedEffect
        val studentParentId = allTags.firstOrNull { it.slug == "student-events" }?.id
        if (studentParentId != null) {
            tagList = allTags.filter { it.parentId == studentParentId }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Screen Title.
        Text(text = "Student Events Planning for $email", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // List of discussions.
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(tagList) { tag ->
                TagCard(tag = tag) {
                    val tagUrl = "http://129.154.249.30:8080/t/${tag.slug}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tagUrl))
                    context.startActivity(intent)
                }
            }
        }

        // Creation UI (pinned at the bottom).
        Column(modifier = Modifier.fillMaxWidth()) {

            // **Simple Dropdown with a Button**
            Text(text = "Select Your Year:", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { expanded = !expanded }
                ) {
                    Text(text = selectedYear)
                }
                // The dropdown menu anchored to this Row
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    yearOptions.forEach { year ->
                        DropdownMenuItem(
                            text = { Text(year) },
                            onClick = {
                                selectedYear = year
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Event name input.
            OutlinedTextField(
                value = eventName,
                onValueChange = { eventName = it },
                label = { Text("Event Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Select Tag Color:", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            CustomColorPickerRows(
                primaryColors = primaryColors,
                additionalShades = additionalShades,
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        val allTags = flarumTagRepository.getTags()
                        val parentId = allTags?.firstOrNull { it.slug == "student-events" }?.id
                        if (parentId == null) {
                            Toast.makeText(context, "Student parent tag missing. Create it first.", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        // Generate final title as "FE - [Event Name]".
                        val finalTitle = if (eventName.isNotBlank()) {
                            "$selectedYear - $eventName"
                        } else {
                            Toast.makeText(context, "Please enter an event name.", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        val newTag = flarumTagRepository.createTag(
                            name = finalTitle,
                            color = selectedColor.toHexString(),
                            parentTagId = parentId
                        )

                        if (newTag != null) {
                            Toast.makeText(context, "Discussion '${newTag.name}' created", Toast.LENGTH_SHORT).show()
                            // Refresh list.
                            flarumTagRepository.getTags()?.let { updatedTags ->
                                tagList = updatedTags.filter { it.parentId == parentId }
                            }
                            eventName = ""
                        } else {
                            Toast.makeText(context, "Failed to create discussion", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Event Discussion")
            }
        }
    }
}


