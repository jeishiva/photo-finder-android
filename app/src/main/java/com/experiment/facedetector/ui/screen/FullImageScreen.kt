package com.experiment.facedetector.ui.screen

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.experiment.facedetector.R
import com.experiment.facedetector.common.LogManager
import com.experiment.facedetector.domain.entities.FaceTag
import com.experiment.facedetector.domain.entities.FullImageWithFaces
import com.experiment.facedetector.image.ImageCoordinateHelper
import com.experiment.facedetector.ui.widgets.AppBar
import com.experiment.facedetector.ui.widgets.AppCircularProgressIndicator
import com.experiment.facedetector.ui.widgets.AppFullScreenImage
import com.experiment.facedetector.viewmodel.FullImageViewModel
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullImageScreen(navController: NavHostController) {
    val viewModel: FullImageViewModel = koinViewModel()
    val fullImageResult = viewModel.fullImageResult.collectAsState()
    var editingFaceId by remember { mutableStateOf<String?>(null) }
    var currentInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = { FullImageTopBar(navController) },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        FullImageContent(
            innerPadding = innerPadding,
            fullImageResult = fullImageResult.value,
            editingFaceId = editingFaceId,
            onFaceClick = { face ->
                editingFaceId = face.id
                currentInput = face.tag
            },
            onTagChanged = { face, newTag ->
                editingFaceId = null
                currentInput = ""
                viewModel.saveFaceTag(face, newTag)
            })
    }
}

// Top Bar Component
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FullImageTopBar(navController: NavHostController) {
    AppBar(
        title = stringResource(R.string.full_image),
        onBackClicked = { navController.popBackStack() }
    )
}

@Composable
private fun FullImageContent(
    innerPadding: PaddingValues,
    fullImageResult: FullImageWithFaces?,
    editingFaceId: String?,
    onFaceClick: (FaceTag) -> Unit,
    onTagChanged: (FaceTag, String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding), contentAlignment = Alignment.Center
    ) {
        when (fullImageResult) {
            null -> AppCircularProgressIndicator()
            else -> {
                FullImageWithFaceOverlay(
                    bitmap = fullImageResult.image,
                    faceTags = fullImageResult.faces,
                    editingFaceId = editingFaceId,
                    onFaceClick = onFaceClick,
                    onTagChanged = onTagChanged
                )
            }
        }
    }
}

@Composable
fun FullImageWithFaceOverlay(
    bitmap: Bitmap,
    faceTags: List<FaceTag>,
    editingFaceId: String?,
    onFaceClick: (FaceTag) -> Unit,
    onTagChanged: (FaceTag, String) -> Unit
) {
    val imageBitmap = bitmap.asImageBitmap()
    val density = LocalDensity.current

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val containerWidthPx = with(density) { this@BoxWithConstraints.maxWidth.toPx() }
        val containerHeightPx = with(density) { this@BoxWithConstraints.maxHeight.toPx() }

        val coordinateHelper =
            remember(containerWidthPx, containerHeightPx, bitmap.width, bitmap.height) {
                ImageCoordinateHelper(
                    containerWidthPx,
                    containerHeightPx,
                    bitmap.width.toFloat(),
                    bitmap.height.toFloat()
                )
            }

        AppFullScreenImage(imageBitmap)

        faceTags.forEach { face ->
            key(face.id) {
                val (leftPx, topPx) = coordinateHelper.imageToContainerCoords(
                    face.left.toFloat(), face.top.toFloat()
                )
                val widthPx = coordinateHelper.scaleWidth(face.width.toFloat())
                val heightPx = coordinateHelper.scaleHeight(face.height.toFloat())

                val coordinates = with(density) {
                    FaceTagCoordinates(
                        left = leftPx.toDp(),
                        top = topPx.toDp(),
                        width = widthPx.toDp(),
                        height = heightPx.toDp()
                    )
                }

                FaceTagItem(
                    face = face,
                    coordinates = coordinates,
                    isEditing = editingFaceId == face.id,
                    onFaceClick = onFaceClick,
                    onTagChanged = onTagChanged
                )
            }
        }
    }
}

// Individual Face Tag Component
@Composable
private fun FaceTagItem(
    face: FaceTag,
    coordinates: FaceTagCoordinates,
    isEditing: Boolean,
    onFaceClick: (FaceTag) -> Unit,
    onTagChanged: (FaceTag, String) -> Unit
) {
    Column(
        modifier = Modifier.absoluteOffset(
            x = coordinates.left, y = coordinates.top
        ), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FaceBox(
            width = coordinates.width,
            height = coordinates.height,
            isEditing = isEditing,
            onClick = { onFaceClick(face) }) {
            if (isEditing) {
                Box(
                    modifier = Modifier
                        .width(coordinates.width)
                        .height(coordinates.height)
                ) {
                    EditableTagInput(
                        initialTag = face.tag,
                        onTagSubmit = { newTag -> onTagChanged(face, newTag) })
                }
            }
        }
        if (face.tag.isNotEmpty() && !isEditing) {
            StaticTagDisplay(
                tag = face.tag, maxWidth = coordinates.width
            )
        }
    }
}

// Face Box Component
@Composable
private fun FaceBox(
    width: Dp,
    height: Dp,
    isEditing: Boolean,
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .background(
                color = if (isEditing) Color(0x40808080) else Color.Transparent
            )
            .border(2.dp, Color.Cyan)
            .clickable(onClick = onClick), content = content
    )
}

// Editable Tag Input Component
@Composable
private fun EditableTagInput(
    initialTag: String, onTagSubmit: (String) -> Unit
) {
    var tagText by remember { mutableStateOf(initialTag) }
    TextField(
        value = tagText,
        onValueChange = { tagText = it },
        singleLine = true,
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null
            ) { /* Consume click to prevent propagation to parent */ },
        textStyle = MaterialTheme.typography.labelMedium,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0x80000000),
            unfocusedContainerColor = Color(0x80000000),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        keyboardActions = KeyboardActions(onDone = { onTagSubmit(tagText) }),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
    )
}

@Composable
fun StaticTagDisplay(
    tag: String, maxWidth: Dp
) {
    Text(
        text = tag,
        style = MaterialTheme.typography.bodySmall.copy(
            fontSize = 10.sp
        ),
        color = Color.White,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .widthIn(max = maxWidth)
            .background(Color(0x80000000), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

data class FaceTagCoordinates(
    val left: Dp, val top: Dp, val width: Dp, val height: Dp
)