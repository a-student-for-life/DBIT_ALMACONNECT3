package com.example.dbit_almaconnect3.ui.screens.features

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import kotlinx.coroutines.launch

// Helper extension to convert Color to hex.
fun Color.toHex(): String {
    return String.format("#%06X", 0xFFFFFF and this.toArgb())
}

// Display a tag as a rounded colored card.
@Composable
fun TagItem(tag: Tag, onClick: () -> Unit) {
    val backgroundColor = try {
        Color(android.graphics.Color.parseColor(tag.color))
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

// Improved color picker with two horizontal rows.
@Composable
fun ColorPickerRows(
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
                ColorSwatch(color = color, selected = (color == selectedColor), onClick = { onColorSelected(color) })
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Additional Shades", fontSize = 14.sp)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(additionalShades) { color ->
                ColorSwatch(color = color, selected = (color == selectedColor), onClick = { onColorSelected(color) })
            }
        }
    }
}

// A single circular color swatch.
@Composable
fun ColorSwatch(
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

@Composable
fun MentorshipScreen(email: String, navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val flarumTagRepository = remember { FlarumTagRepository() }

    var tagName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFFFFFFFF)) }
    var tagList by remember { mutableStateOf<List<Tag>>(emptyList()) }

    // Define primary and additional colors.
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

    // Fetch all tags when the screen loads.
    LaunchedEffect(Unit) {
        flarumTagRepository.getTags()?.let { allTags ->
            // Filter to only show tags under the primary mentorship tag.
            val primaryId = flarumTagRepository.getPrimaryMentorshipTagId()
            tagList = if (primaryId != null) {
                allTags.filter { it.parentId == primaryId }
            } else {
                emptyList()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Mentorship for $email", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Existing Tags:", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(tagList) { tag ->
                TagItem(tag = tag) {
                    val tagUrl = "http://129.154.249.30:8080/t/${tag.slug}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tagUrl))
                    context.startActivity(intent)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = tagName,
            onValueChange = { tagName = it },
            label = { Text("New Tag Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Select Tag Color:", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        ColorPickerRows(
            primaryColors = primaryColors,
            additionalShades = additionalShades,
            selectedColor = selectedColor,
            onColorSelected = { selectedColor = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    val parentId = flarumTagRepository.getPrimaryMentorshipTagId()
                    val newTag = flarumTagRepository.createTag(tagName, selectedColor.toHex(), parentId)
                    if (newTag != null) {
                        Toast.makeText(context, "Tag '${newTag.name}' created", Toast.LENGTH_SHORT).show()
                        // Refresh the tag list.
                        flarumTagRepository.getTags()?.let { allTags ->
                            val primaryId = flarumTagRepository.getPrimaryMentorshipTagId()
                            tagList = if (primaryId != null) {
                                allTags.filter { it.parentId == primaryId }
                            } else {
                                emptyList()
                            }
                        }
                        tagName = ""
                    } else {
                        Toast.makeText(context, "Failed to create tag", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Tag")
        }
    }
}

// Wrappers to retain existing file names.
@Composable
fun StudentMentorshipScreen(email: String, navController: NavController) {
    MentorshipScreen(email, navController)
}

@Composable
fun AlumniMentorshipScreen(email: String, navController: NavController) {
    MentorshipScreen(email, navController)
}
