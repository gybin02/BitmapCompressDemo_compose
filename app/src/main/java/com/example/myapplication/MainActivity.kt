package com.example.myapplication

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.io.ByteArrayOutputStream
import java.io.IOException

class MainActivity : ComponentActivity() {
    private val PICK_IMAGE_REQUEST_CODE: Int = 100

//    var selectedImage: Bitmap? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                ImageQualityScreenContent(onSaveImageClicked = {selectedImage, imageQuality ->
                    // 处理保存图片到相册的逻辑
                    if (selectedImage != null) {
                        // 保存图片到相册
                        saveBitmapToGallery(this, selectedImage, imageQuality)
                        Toast.makeText(this,"保存成功",Toast.LENGTH_LONG).show()
                    }
                })
            }
        }
    }

    fun test() {
        val imageSelectionScreen = findViewById<ImageQualityScreenContent>(androidx.core.R.id.action_image)

    }


//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            // 获取用户选择的图片
//            val imageUri = data?.data
//            if (imageUri != null) {
//                val selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
//                selectedImage = selectedBitmap // 更新选定的图片状态
//            }
//        }
//    }

//    fun gotoGallery() {
//        // 打开图片选择器，获取用户选择的图片
//        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.type = "image/*"
//        ActivityCompat.startActivityForResult(this, intent, PICK_IMAGE_REQUEST_CODE, null)
//    }


    @RequiresApi(Build.VERSION_CODES.Q)
    @Composable
    fun ImageQualityScreenContent(onSaveImageClicked: (Bitmap?,Int) -> Unit) {
        var imageQuality by remember { mutableStateOf(50) }
        var selectedImage: Bitmap? by remember { mutableStateOf(null) }

        val context = LocalContext.current
        val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { result: Uri? ->
            result?.let { uri ->
                val contentResolver = context.contentResolver
//                val bitmap =
                //MediaStore.Images.Media.getBitmap(contentResolver, uri)
//                    contentResolver.loadThumbnail(uri, Size(256, 256), null)
                try {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    selectedImage = bitmap
                } catch (e: Exception) {
                    // Handle exceptions
                }
            }
        }


        ImageQualityScreen(
            onSliderValueChanged = { newValue ->
                imageQuality = newValue
            },
            onChooseImageClicked = { galleryLauncher.launch("image/*") },
            onSaveImageClicked = { onSaveImageClicked(selectedImage,imageQuality) },
            imageQuality = imageQuality,
            selectedImage = selectedImage
        )
    }
}

fun createBitmap(selectedImage: Bitmap, quality: Int): Bitmap {
    val outputStream = ByteArrayOutputStream()
    selectedImage.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
    val byteArray = outputStream.toByteArray()
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}

fun saveBitmapToGallery(context: Context, bitmap: Bitmap?, imageQuality: Int) {
    // 使用 MediaStore 将 Bitmap 保存到相册
    bitmap ?: return
    val displayName = "Image_${System.currentTimeMillis()}.jpg"
    val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    val imageDetails = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }

    val imageUri = context.contentResolver.insert(imageCollection, imageDetails)

    try {
        context.contentResolver.openOutputStream(imageUri ?: return)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, imageQuality, outputStream)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Composable
fun ImageQualityScreen(
    onSliderValueChanged: (Int) -> Unit,
    onChooseImageClicked: () -> Unit,
    onSaveImageClicked: () -> Unit,
    imageQuality: Int,
    selectedImage: Bitmap? // 新增一个用于显示图片的参数
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // 选择图片按钮
        Button(
            onClick = { onChooseImageClicked() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "选择图片")
        }
        // 添加 ImageView 显示从相册选择的图片
        if (selectedImage != null) {
            Image(
                bitmap = selectedImage.asImageBitmap(),
                contentDescription = null, // 可以设置描述
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // 设置图片高度
            )
        }

        // 滑动条
        Slider(
            value = imageQuality.toFloat(),
            onValueChange = { value ->
                onSliderValueChanged(value.toInt())
            },
            valueRange = 0f..100f
        )

        // 显示图片大小
        Text(
            text = "质量因子: $imageQuality",
            fontSize = 16.sp
        )

        // 显示图片大小
        Text(
            text = "图片大小: ${calculateImageSize(selectedImage, imageQuality)} KB",
            fontSize = 16.sp
        )


        // 保存图片按钮
        Button(
            onClick = { onSaveImageClicked() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "保存图片到相册")
        }
    }
}

fun calculateImageSize(bitmap: Bitmap?, quality: Int): Int {
    bitmap ?: return 0
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
    val sizeInBytes = outputStream.size()
    outputStream.close()
    return sizeInBytes / 1024 // 返回大小以 KB 为单位
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}