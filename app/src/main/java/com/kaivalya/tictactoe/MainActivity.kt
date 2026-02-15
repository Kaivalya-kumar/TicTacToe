package com.kaivalya.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaivalya.tictactoe.ui.theme.TicTacToeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            var showSplash by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(2000)
                showSplash = false
            }
            
            TicTacToeTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showSplash) {
                        SplashScreen()
                    } else {
                        TicTacToeGame(
                            isDarkTheme = isDarkTheme,
                            onThemeToggle = { isDarkTheme = !isDarkTheme }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Tic Tac Toe",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Made by Kaivalya Kumar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
        }
    }
}

@Composable
fun TicTacToeGame(isDarkTheme: Boolean, onThemeToggle: () -> Unit) {
    var board by remember { mutableStateOf(List(9) { "" }) }
    var isPlayerTurn by remember { mutableStateOf(true) } // true for X, false for O
    var isPvP by remember { mutableStateOf(false) }
    var winner by remember { mutableStateOf<String?>(null) }
    var winningLine by remember { mutableStateOf<List<Int>?>(null) }
    val scope = rememberCoroutineScope()
    val lineProgress = remember { Animatable(0f) }

    val colorX = Color(0xFF2196F3)
    val colorO = Color(0xFFF44336)

    LaunchedEffect(winner) {
        if (winner != null && winner != "Draw") {
            lineProgress.animateTo(1f, animationSpec = tween(600))
        } else {
            lineProgress.snapTo(0f)
        }
    }

    fun checkWinner(currentBoard: List<String>): Pair<String?, List<Int>?> {
        val lines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6)
        )
        for (line in lines) {
            if (currentBoard[line[0]].isNotEmpty() &&
                currentBoard[line[0]] == currentBoard[line[1]] &&
                currentBoard[line[0]] == currentBoard[line[2]]
            ) return Pair(currentBoard[line[0]], line)
        }
        if (currentBoard.all { it.isNotEmpty() }) return Pair("Draw", null)
        return Pair(null, null)
    }

    // Minimax Algorithm for better AI
    fun minimax(currentBoard: MutableList<String>, depth: Int, isMaximizing: Boolean): Int {
        val (res, _) = checkWinner(currentBoard)
        if (res == "O") return 10 - depth
        if (res == "X") return depth - 10
        if (res == "Draw") return 0

        if (isMaximizing) {
            var bestScore = Int.MIN_VALUE
            for (i in 0..8) {
                if (currentBoard[i].isEmpty()) {
                    currentBoard[i] = "O"
                    val score = minimax(currentBoard, depth + 1, false)
                    currentBoard[i] = ""
                    bestScore = maxOf(score, bestScore)
                }
            }
            return bestScore
        } else {
            var bestScore = Int.MAX_VALUE
            for (i in 0..8) {
                if (currentBoard[i].isEmpty()) {
                    currentBoard[i] = "X"
                    val score = minimax(currentBoard, depth + 1, true)
                    currentBoard[i] = ""
                    bestScore = minOf(score, bestScore)
                }
            }
            return bestScore
        }
    }

    fun findBestMove(currentBoard: List<String>): Int {
        var bestScore = Int.MIN_VALUE
        var move = -1
        val boardCopy = currentBoard.toMutableList()
        for (i in 0..8) {
            if (boardCopy[i].isEmpty()) {
                boardCopy[i] = "O"
                val score = minimax(boardCopy, 0, false)
                boardCopy[i] = ""
                if (score > bestScore) {
                    bestScore = score
                    move = i
                }
            }
        }
        return move
    }

    fun computerMove() {
        scope.launch {
            delay(800)
            val move = findBestMove(board)
            if (move != -1 && winner == null) {
                val newBoard = board.toMutableList()
                newBoard[move] = "O"
                board = newBoard
                val (res, line) = checkWinner(board)
                winner = res
                winningLine = line
                isPlayerTurn = true
            }
        }
    }

    fun onCellClick(index: Int) {
        if (board[index].isEmpty() && winner == null && (isPlayerTurn || isPvP)) {
            val symbol = if (isPlayerTurn) "X" else "O"
            val newBoard = board.toMutableList()
            newBoard[index] = symbol
            board = newBoard
            val (res, line) = checkWinner(board)
            winner = res
            winningLine = line

            if (winner == null) {
                if (isPvP) {
                    isPlayerTurn = !isPlayerTurn
                } else {
                    isPlayerTurn = false
                    computerMove()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tic Tac Toe",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(
                onClick = onThemeToggle,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                    contentDescription = "Toggle Theme"
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Mode Selector Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    "Computer",
                    fontWeight = if (!isPvP) FontWeight.Bold else FontWeight.Normal,
                    color = if (!isPvP) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Switch(
                    checked = isPvP,
                    onCheckedChange = {
                        isPvP = it
                        board = List(9) { "" }
                        winner = null
                        winningLine = null
                        isPlayerTurn = true
                    }
                )
                Text(
                    "PvP",
                    fontWeight = if (isPvP) FontWeight.Bold else FontWeight.Normal,
                    color = if (isPvP) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Status
        val statusText = when {
            winner == "Draw" -> "Draw Game!"
            winner != null -> if (isPvP) "Player $winner Wins!" else if (winner == "X") "You Won!" else "AI Won!"
            isPlayerTurn -> if (isPvP) "Player X's Turn" else "Your Turn (X)"
            else -> if (isPvP) "Player O's Turn" else "AI is thinking..."
        }
        
        Text(
            text = statusText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = if (winner != null) (if (winner == "X") colorX else if (winner == "O") colorO else MaterialTheme.colorScheme.onSurface) else MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Grid
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .shadow(12.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                for (row in 0..2) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (col in 0..2) {
                            val index = row * 3 + col
                            CellModern(
                                value = board[index],
                                onClick = { onCellClick(index) },
                                colorX = colorX,
                                colorO = colorO,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            if (winningLine != null) {
                val lineStrokeColor by animateColorAsState(
                    targetValue = if (winner == "X") colorX else colorO,
                    label = "LineColor"
                )
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val start = winningLine!![0]
                    val end = winningLine!![2]

                    val startOffset = Offset(
                        x = (start % 3) * (size.width / 3) + (size.width / 6),
                        y = (start / 3) * (size.height / 3) + (size.height / 6)
                    )
                    val endOffset = Offset(
                        x = (end % 3) * (size.width / 3) + (size.width / 6),
                        y = (end / 3) * (size.height / 3) + (size.height / 6)
                    )

                    drawLine(
                        color = lineStrokeColor,
                        start = startOffset,
                        end = Offset(
                            x = startOffset.x + (endOffset.x - startOffset.x) * lineProgress.value,
                            y = startOffset.y + (endOffset.y - startOffset.y) * lineProgress.value
                        ),
                        strokeWidth = 12.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Reset Button
        Button(
            onClick = {
                board = List(9) { "" }
                winner = null
                winningLine = null
                isPlayerTurn = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Restart Match", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun CellModern(
    value: String,
    onClick: () -> Unit,
    colorX: Color,
    colorO: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)).togetherWith(fadeOut(animationSpec = tween(300)))
            },
            label = "CellAnim"
        ) { targetValue ->
            Text(
                text = targetValue,
                fontSize = 54.sp,
                fontWeight = FontWeight.Black,
                color = if (targetValue == "X") colorX else colorO
            )
        }
    }
}
