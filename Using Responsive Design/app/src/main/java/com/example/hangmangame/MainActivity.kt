package com.example.hangmangame

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import com.example.hangmangame.ui.theme.HangmanGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HangmanGameTheme {
                HangmanGameScreen()
            }
        }
    }
}

@Composable
fun HangmanGameScreen() {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT

    var wordToGuess by remember { mutableStateOf(getRandomWord().uppercase()) }
    var guessedLetters by remember { mutableStateOf(setOf<Char>()) }
    var remainingTurns by remember { mutableIntStateOf(6) }
    var wrongGuesses by remember { mutableIntStateOf(0) }
    var hintClickCount by remember { mutableIntStateOf(0) }
    var hintText by remember { mutableStateOf("") }
    var gameOverMessage by remember { mutableStateOf<String?>(null) }

    val disabledLetters = guessedLetters
    val hangmanImage = painterResource(id = getHangmanImageResource(wrongGuesses))

    LaunchedEffect(remainingTurns, guessedLetters) {
        if (remainingTurns <= 0) {
            gameOverMessage = "Lose! The word is $wordToGuess."
        } else {
            var isWin = false
            for (letter in wordToGuess) {
                if (letter !in guessedLetters) {
                    isWin = true
                    break
                }
            }
            if (!isWin) {
                gameOverMessage = "Win!"
            }
        }
    }

    if (gameOverMessage != null) {
        LaunchedEffect(gameOverMessage) {
            Toast.makeText(context, gameOverMessage, Toast.LENGTH_LONG).show()
            wordToGuess = getRandomWord().uppercase()
            guessedLetters = setOf()
            remainingTurns = 6
            wrongGuesses = 0
            hintClickCount = 0
            hintText = ""
            gameOverMessage = null
        }
    }


    if (isPortrait) {
        PortraitLayout(
            wordDisplay = getWordDisplay(wordToGuess, guessedLetters),
            hangmanImage = hangmanImage,
            onLetterClicked = { letter ->
                guessedLetters = guessedLetters + letter
                if (!wordToGuess.contains(letter)) {
                    remainingTurns--
                    wrongGuesses = 6 - remainingTurns
                }
            },
            disabledLetters = disabledLetters,
            onNewGame = {
                wordToGuess = getRandomWord().uppercase()
                guessedLetters = setOf()
                remainingTurns = 6
                wrongGuesses = 0
            }
        )
    } else {
        LandscapeLayout(
            wordDisplay = getWordDisplay(wordToGuess, guessedLetters),
            hangmanImage = hangmanImage,
            onLetterClicked = { letter ->
                guessedLetters = guessedLetters + letter
                if (!wordToGuess.contains(letter)) {
                    remainingTurns--
                    wrongGuesses = 6 - remainingTurns
                }
            },
            disabledLetters = disabledLetters,
            onNewGame = {
                wordToGuess = getRandomWord().uppercase()
                guessedLetters = setOf()
                remainingTurns = 6
                wrongGuesses = 0
                hintClickCount = 0
                hintText = ""
            },
            onShowHint = {
                hintText = showHint(
                    context,
                    wordToGuess,
                    guessedLetters,
                    remainingTurns,
                    hintClickCount,
                    onLettersDisabled = { letters ->
                        guessedLetters = guessedLetters + letters
                        remainingTurns--
                        wrongGuesses = 6 - remainingTurns
                    }
                )
                hintClickCount++
            },
            hintText = hintText
        )
    }
}

@Composable
fun PortraitLayout(
    wordDisplay: String,
    hangmanImage: Painter,
    onLetterClicked: (Char) -> Unit,
    disabledLetters: Set<Char>,
    onNewGame: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CHOOSE A LETTER",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 8.dp)
        )

        LetterButtonsGrid(
            onLetterClicked = onLetterClicked,
            disabledLetters = disabledLetters
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = wordDisplay,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = hangmanImage,
            contentDescription = "Hangman Image",
            modifier = Modifier.size(width = 283.dp, height = 349.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNewGame,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("New Game")
        }
    }
}

@Composable
fun LandscapeLayout(
    wordDisplay: String,
    hangmanImage: Painter,
    onLetterClicked: (Char) -> Unit,
    disabledLetters: Set<Char>,
    onNewGame: () -> Unit,
    onShowHint: () -> Unit,
    hintText: String
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(5f)
                .padding(end = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = wordDisplay,
                fontSize = 28.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(156.dp)
                    .padding(8.dp)
            )

            Text(
                text = "CHOOSE A LETTER",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 8.dp)
            )

            LetterButtonsGrid(
                onLetterClicked = onLetterClicked,
                disabledLetters = disabledLetters
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Image(
            painter = hangmanImage,
            contentDescription = "Hangman Image",
            modifier = Modifier
                .weight(4f)
                .size(width = 210.dp, height = 395.dp)
        )

        Column(
            modifier = Modifier
                .weight(3f)
                .padding(start = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onNewGame,
                modifier = Modifier
                    .padding(8.dp)
                    .width(120.dp)
            ) {
                Text("New Game")
            }

            Button(
                onClick = onShowHint,
                modifier = Modifier
                    .padding(8.dp)
                    .width(120.dp)
            ) {
                Text("Hint")
            }

            if (hintText.isNotEmpty()) {
                Text(
                    text = hintText,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                )
            }
        }
    }
}

@Composable
fun LetterButtonsGrid(
    onLetterClicked: (Char) -> Unit,
    disabledLetters: Set<Char>
) {
    val letters = ('A'..'Z').toList()
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        items(letters) { letter ->
            Button(
                onClick = { onLetterClicked(letter) },
                enabled = !disabledLetters.contains(letter),
                modifier = Modifier
                    .size(50.dp)
                    .padding(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Gray,
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    letter.toString(),
                    fontSize = 24.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

fun getWordDisplay(wordToGuess: String, guessedLetters: Set<Char>): String {
    var display = ""
    for (i in wordToGuess.indices) {
        val letter = wordToGuess[i]
        if (guessedLetters.contains(letter)) {
            display += letter
        } else {
            display += "_"
        }
        if (i != wordToGuess.length - 1) {
            display += " "
        }
    }
    return display
}

fun getRandomWord(): String {
    val wordList = listOf(
        "CAT"
    )
    val randomIndex = (Math.random() * wordList.size).toInt()
    return wordList[randomIndex]
}


fun getHangmanImageResource(wrongGuesses: Int): Int {
    when (wrongGuesses) {
        0 -> return R.drawable.hangman0
        1 -> return R.drawable.hangman1
        2 -> return R.drawable.hangman2
        3 -> return R.drawable.hangman3
        4 -> return R.drawable.hangman4
        5 -> return R.drawable.hangman5
        else -> return R.drawable.hangman6
    }
}

fun showHint(
    context: android.content.Context,
    wordToGuess: String,
    guessedLetters: Set<Char>,
    remainingTurns: Int,
    hintClickCount: Int,
    onLettersDisabled: (Set<Char>) -> Unit
): String {
    val wordHints = mapOf(
        "CAT" to "ANIMAL",
        "DOG" to "ANIMAL",
        "SKY" to "NATURE",
        "HELLO" to "GREETING",
        "APPLE" to "FOOD",
        "HOUSE" to "BUILDING",
        "LION" to "ANIMAL",
        "BIRD" to "ANIMAL",
        "TREE" to "NATURE",
        "FISH" to "ANIMAL",
        "BOOK" to "OBJECT",
        "STAR" to "NATURE",
        "MOON" to "NATURE",
        "SUN" to "NATURE",
        "RAIN" to "NATURE",
        "WIND" to "NATURE",
        "FIRE" to "ELEMENT",
        "ICE" to "ELEMENT",
        "EARTH" to "ELEMENT",
        "WATER" to "ELEMENT"
    )

    when (hintClickCount) {
        0 -> {
            return wordHints[wordToGuess] ?: ""
        }
        1 -> {
            if (remainingTurns <= 1) {
                Toast.makeText(context, "Hint not available", Toast.LENGTH_SHORT).show()
                return wordHints[wordToGuess] ?: ""
            } else {
                val incorrectLetters = ('A'..'Z').filter { it !in wordToGuess && it !in guessedLetters }
                if (incorrectLetters.isNotEmpty()) {
                    val lettersToDisable = incorrectLetters.shuffled().take((incorrectLetters.size + 1) / 2).toSet()
                    onLettersDisabled(lettersToDisable)
                    Toast.makeText(context, "Hint: Disabled some incorrect letters", Toast.LENGTH_SHORT).show()
                    return wordHints[wordToGuess] ?: ""
                } else {
                    Toast.makeText(context, "No letters to disable", Toast.LENGTH_SHORT).show()
                    return wordHints[wordToGuess] ?: ""
                }
            }
        }
        2 -> {
            if (remainingTurns <= 1) {
                Toast.makeText(context, "Hint not available", Toast.LENGTH_SHORT).show()
                return wordHints[wordToGuess] ?: ""
            } else {
                val vowels = listOf('A', 'E', 'I', 'O', 'U')
                val lettersToDisable = vowels.shuffled().toSet()

                val vowelsInWord = vowels.filter { it in wordToGuess && it !in guessedLetters }
                if (vowelsInWord.isNotEmpty()) {
                    onLettersDisabled(lettersToDisable)
                    Toast.makeText(context, "Hint: Revealed all vowels", Toast.LENGTH_SHORT).show()
                    return wordHints[wordToGuess] ?: ""
                } else {
                    Toast.makeText(context, "No unrevealed vowels", Toast.LENGTH_SHORT).show()
                    return wordHints[wordToGuess] ?: ""
                }
            }
        }
        else -> {
            Toast.makeText(context, "No more hints available", Toast.LENGTH_SHORT).show()
            return wordHints[wordToGuess] ?: ""
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    HangmanGameTheme {
        HangmanGameScreen()
    }
}
