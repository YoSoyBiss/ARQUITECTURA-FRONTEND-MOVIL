package com.example.clientemovil.ui.screens.sales

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.clientemovil.models.SaleResponse
import com.example.clientemovil.network.node.NodeRetrofitClient
// Importa tus colores personalizados
import com.example.clientemovil.ui.theme.BlackText
import com.example.clientemovil.ui.theme.BrownBorder
import com.example.clientemovil.ui.theme.CreamBackground
import com.example.clientemovil.ui.theme.WhiteCard
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(navController: NavController, canEdit: Boolean) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var sales by remember { mutableStateOf<List<SaleResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    var showDialog by remember { mutableStateOf(false) }
    var selectedSaleForDialog by remember { mutableStateOf<SaleResponse?>(null) }

    suspend fun loadSales() {
        isLoading = true
        try {
            val response = NodeRetrofitClient.api.getAllSales()
            if (response.isSuccessful) {
                sales = response.body() ?: emptyList()
            } else {
                val errorMsg = "Error al cargar ventas: ${response.code()} ${response.message()}"
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                Log.e("SalesScreen", "$errorMsg - ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Excepción al cargar ventas: ${e.message}", Toast.LENGTH_LONG).show()
            Log.e("SalesScreen", "Error al cargar ventas", e)
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            loadSales()
        }
    }

    Scaffold(
        containerColor = CreamBackground,
        topBar = {
            TopAppBar(
                title = { Text(text = "Gestión de Ventas", color = WhiteCard) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrownBorder
                )
            )
        },
        floatingActionButton = {
            if (canEdit) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate("sale_form/new")
                    },
                    containerColor = BrownBorder,
                    contentColor = WhiteCard
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir Venta")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> CircularProgressIndicator(
                    Modifier.align(Alignment.Center),
                    color = BrownBorder
                )
                sales.isEmpty() -> Text(
                    "No hay ventas registradas.",
                    Modifier.align(Alignment.Center),
                    color = BlackText
                )
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(sales, key = { sale -> sale.id ?: UUID.randomUUID().toString() }) { sale ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedSaleForDialog = sale
                                        showDialog = true
                                    },
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = WhiteCard // Fondo de la tarjeta
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                ) {
                                    Text(
                                        text = "ID Venta: ${sale.id?.takeLast(6) ?: "N/A"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BlackText.copy(alpha = 0.7f) // Un poco más suave
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Total: $${String.format(Locale.US, "%.2f", sale.total)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = BrownBorder // Destacar el total
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Cliente: ${sale.userId?.name ?: "No especificado"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = BlackText
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Fecha: ${sale.date?.let { formatter.format(it) } ?: "N/A"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BlackText.copy(alpha = 0.8f)
                                    )

                                    sale.details?.let { detailsList ->
                                        if (detailsList.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                "Productos:",
                                                style = MaterialTheme.typography.labelMedium,
                                                modifier = Modifier.padding(bottom = 4.dp),
                                                color = BrownBorder
                                            )
                                            detailsList.forEach { detail ->
                                                val calculatedSubtotal = detail.quantity * detail.unitPrice
                                                Text(
                                                    text = "  - ID Prod: ${detail.productId}, Cant: ${detail.quantity}, " +
                                                            "P.Unit: $${String.format(Locale.US, "%.2f", detail.unitPrice)}, " +
                                                            "Subt: $${String.format(Locale.US, "%.2f", calculatedSubtotal)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = BlackText.copy(alpha = 0.9f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDialog && selectedSaleForDialog != null) {
            val saleToDisplay = selectedSaleForDialog!!
            AlertDialog(
                containerColor = WhiteCard,
                onDismissRequest = { showDialog = false },
                title = { Text("Detalle de Venta", color = BrownBorder) },
                text = {
                    LazyColumn {
                        item { Text("ID Venta: ${saleToDisplay.id?.takeLast(6) ?: "N/A"}", color = BlackText) }
                        item { Text("Total: $${String.format(Locale.US, "%.2f", saleToDisplay.total)}", color = BrownBorder, style = MaterialTheme.typography.titleMedium) }
                        item { Text("Cliente: ${saleToDisplay.userId?.name ?: "No especificado"}", color = BlackText) }
                        item { Text("Fecha: ${saleToDisplay.date?.let { formatter.format(it) } ?: "N/A"}", color = BlackText) }

                        saleToDisplay.details?.let { detailsList ->
                            if (detailsList.isNotEmpty()) {
                                item { Spacer(modifier = Modifier.height(8.dp)) }
                                item { Text("Productos:", style = MaterialTheme.typography.labelMedium, color = BrownBorder) }
                                items(detailsList) { detail ->
                                    val calculatedSubtotal = detail.quantity * detail.unitPrice
                                    Text(
                                        "  - ID Prod: ${detail.productId}, Cant: ${detail.quantity}, " +
                                                "P.Unit: $${String.format(Locale.US, "%.2f", detail.unitPrice)}, " +
                                                "Subt: $${String.format(Locale.US, "%.2f", calculatedSubtotal)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BlackText.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            generateAndSavePdf(context, saleToDisplay, formatter)
                            showDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrownBorder,
                            contentColor = WhiteCard
                        )
                    ) { Text("Imprimir Ticket") }
                },
                dismissButton = {
                    Button(
                        onClick = { showDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CreamBackground, // Un color diferente para el dismiss
                            contentColor = BrownBorder
                        )
                    ) { Text("Cerrar") }
                }
            )
        }
    }
}

fun generateAndSavePdf(context: Context, sale: SaleResponse, formatter: SimpleDateFormat) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    // Usar android.graphics.Color para Paint
    val blackColor = android.graphics.Color.BLACK
    val darkGrayColor = android.graphics.Color.DKGRAY
    val grayColor = android.graphics.Color.GRAY

    val titlePaint = Paint().apply {
        textSize = 18f
        isFakeBoldText = true
        color = blackColor // Usar color de android.graphics
    }
    val textPaint = Paint().apply {
        textSize = 12f
        color = blackColor // Usar color de android.graphics
    }
    val smallTextPaint = Paint().apply {
        textSize = 10f
        color = darkGrayColor // Usar color de android.graphics
    }
    val boldSmallTextPaint = Paint().apply {
        textSize = 10f
        isFakeBoldText = true
        color = blackColor // Usar color de android.graphics
    }

    var yPosition = 60f
    val xMargin = 40f
    val lineSpacing = 18f
    val sectionSpacing = 25f
    val itemSpacing = 15f

    canvas.drawText("Ticket de Venta", canvas.width / 2f, yPosition, titlePaint.apply { textAlign = Paint.Align.CENTER })
    yPosition += sectionSpacing * 1.5f

    canvas.drawText("ID Venta: ${sale.id?.takeLast(8) ?: "N/A"}", xMargin, yPosition, textPaint)
    yPosition += lineSpacing
    canvas.drawText("Fecha: ${sale.date?.let { formatter.format(it) } ?: "N/A"}", xMargin, yPosition, textPaint)
    yPosition += lineSpacing
    canvas.drawText("Cliente: ${sale.userId?.name ?: "No especificado"}", xMargin, yPosition, textPaint)
    yPosition += sectionSpacing

    canvas.drawText("Productos Detallados:", xMargin, yPosition, boldSmallTextPaint.apply { textSize = 12f} )
    yPosition += lineSpacing
    val productHeaderY = yPosition
    canvas.drawText("Producto (ID)", xMargin, productHeaderY, boldSmallTextPaint)
    canvas.drawText("Cant.", xMargin + 200, productHeaderY, boldSmallTextPaint.apply { textAlign = Paint.Align.RIGHT })
    canvas.drawText("P. Unit.", xMargin + 280, productHeaderY, boldSmallTextPaint.apply { textAlign = Paint.Align.RIGHT })
    canvas.drawText("Subtotal", xMargin + 360, productHeaderY, boldSmallTextPaint.apply { textAlign = Paint.Align.RIGHT })
    yPosition += itemSpacing * 1.5f

    val linePaint = Paint().apply { color = grayColor; strokeWidth = 1f } // Usar color de android.graphics
    canvas.drawLine(xMargin, yPosition - itemSpacing * 0.75f, canvas.width - xMargin, yPosition - itemSpacing * 0.75f, linePaint)

    sale.details?.forEach { detail ->
        val calculatedSubtotal = detail.quantity * detail.unitPrice
        canvas.drawText("ID: ${detail.productId}", xMargin, yPosition, smallTextPaint)
        canvas.drawText("${detail.quantity}", xMargin + 200, yPosition, smallTextPaint.apply { textAlign = Paint.Align.RIGHT })
        canvas.drawText("$${String.format(Locale.US, "%.2f", detail.unitPrice)}", xMargin + 280, yPosition, smallTextPaint.apply { textAlign = Paint.Align.RIGHT })
        canvas.drawText("$${String.format(Locale.US, "%.2f", calculatedSubtotal)}", xMargin + 360, yPosition, smallTextPaint.apply { textAlign = Paint.Align.RIGHT })
        yPosition += itemSpacing
    }
    yPosition += sectionSpacing * 0.5f
    canvas.drawLine(xMargin, yPosition, canvas.width - xMargin, yPosition, linePaint)
    yPosition += sectionSpacing * 0.75f

    canvas.drawText("Total General:", xMargin + 200, yPosition, titlePaint.apply { textAlign = Paint.Align.LEFT; textSize = 14f })
    canvas.drawText("$${String.format(Locale.US, "%.2f", sale.total)}", xMargin + 360, yPosition, titlePaint.apply { textAlign = Paint.Align.RIGHT; textSize = 16f })
    yPosition += sectionSpacing * 2

    canvas.drawText("¡Gracias por su compra!", canvas.width / 2f, yPosition, textPaint.apply { textAlign = Paint.Align.CENTER })

    pdfDocument.finishPage(page)

    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    if (!downloadsDir.exists()) {
        downloadsDir.mkdirs()
    }
    val fileName = "Venta_${sale.id?.takeLast(6) ?: "TICKET"}_${System.currentTimeMillis()}.pdf"
    val file = File(downloadsDir, fileName)

    try {
        FileOutputStream(file).use { fos ->
            pdfDocument.writeTo(fos)
            Toast.makeText(context, "PDF guardado en Descargas: $fileName", Toast.LENGTH_LONG).show()
            Log.i("PdfGenerator", "PDF guardado en: ${file.absolutePath}")
        }
    } catch (e: IOException) {
        Log.e("PdfGenerator", "Error al guardar PDF", e)
        Toast.makeText(context, "Error al guardar PDF: ${e.message}", Toast.LENGTH_LONG).show()
    } finally {
        pdfDocument.close()
    }
}
