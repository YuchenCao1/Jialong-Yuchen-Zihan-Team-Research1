package com.example.hangmangame

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.gridlayout.widget.GridLayout
import androidx.activity.ComponentActivity


class MainActivity : ComponentActivity() {

    private lateinit var wordToGuess: String
    private lateinit var wordDisplay: TextView
    private lateinit var hangmanView: ImageView
    private lateinit var letterButtons: GridLayout
    private var hintTextView: TextView? = null
    private var guessedLetters = mutableSetOf<Char>()
    private var remainingTurns = 6
    private var wrongGuesses = 0
    private var hintClickCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view
        setContentView(R.layout.activity_main)

        // Initialize views
        wordDisplay = findViewById(R.id.wordTextView)
        hangmanView = findViewById(R.id.hangmanView)
        val newGameButton: Button = findViewById(R.id.newGameButton)
        letterButtons = findViewById(R.id.letterButtons)
        hintTextView = findViewById(R.id.hintTextView)
        val hintButton: Button? = findViewById(R.id.hintButton)

        setupLetterButtons(letterButtons)

        if (savedInstanceState == null) {
            // First launch, start a new game
            setupNewGame()
        } else {
            // Restore the game state
            restoreGameState(savedInstanceState)
        }

        newGameButton.setOnClickListener {
            setupNewGame()
        }

        hintButton?.setOnClickListener {
            showHint()
        }
    }

    private fun setupNewGame() {
        wordToGuess = getRandomWord().uppercase()
        guessedLetters.clear()
        remainingTurns = 6
        wrongGuesses = 0
        hintClickCount = 0
        updateHangmanImage()
        updateWordDisplay()

        for (i in 0 until letterButtons.childCount) {
            val button = letterButtons.getChildAt(i) as? Button
            button?.isEnabled = true
        }

        hintTextView?.text = ""
    }

    private fun setupLetterButtons(gridLayout: GridLayout) {
        val alphabet = 'A'..'Z'
        gridLayout.removeAllViews()

        alphabet.forEach { letter ->
            val button = Button(this).apply {
                text = letter.toString()
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 100
                    height = 100
                    setMargins(4, 4, 4, 4)
                }
                setOnClickListener { onLetterClicked(letter) }
            }
            gridLayout.addView(button)
        }
    }

    private fun onLetterClicked(letter: Char) {
        guessedLetters.add(letter)
        updateWordDisplay()
        disableLetterButton(letter)

        if (!wordToGuess.contains(letter)) {
            remainingTurns--
            wrongGuesses = 6 - remainingTurns
            updateHangmanImage()
        }

        checkGameOver()
    }

    private fun updateHangmanImage() {
        val resId = resources.getIdentifier("hangman$wrongGuesses", "drawable", packageName)
        hangmanView.setImageResource(resId)
    }

    private fun updateWordDisplay() {
        wordDisplay.text = wordToGuess.map { if (guessedLetters.contains(it)) it else '_' }.joinToString(" ")
    }

    private fun checkGameOver() {
        if (remainingTurns == 0) {
            showGameOverDialog("Lose! The word is \"$wordToGuess\".")
        } else if (!wordDisplay.text.contains('_')) {
            showGameOverDialog("Win!")
        }
    }

    private fun showGameOverDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Game Over!")
            .setMessage(message)
            .setPositiveButton("Ok") { _, _ -> setupNewGame() }
            .show()
    }

    private fun getRandomWord(): String {
        val wordList = listOf(
            "CAT", "DOG", "SKY", "HELLO", "APPLE", "HOUSE",
            "LION", "BIRD", "TREE", "FISH", "BOOK", "STAR",
            "MOON", "SUN", "RAIN", "WIND", "FIRE", "ICE",
            "EARTH", "WATER"
        )
        return wordList.random()
    }

    private fun showHint() {
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
                val hint = wordHints[wordToGuess] ?: ""
                hintTextView?.text = "Hint: $hint"
                hintClickCount++
            }
            1 -> {
                if (remainingTurns <= 1) {
                    Toast.makeText(this, "Hint not available", Toast.LENGTH_SHORT).show()
                    return
                }

                val incorrectLetters = ('A'..'Z').filter { it !in wordToGuess && it !in guessedLetters }
                if (incorrectLetters.isNotEmpty()) {
                    val lettersToDisable = incorrectLetters.shuffled().take((incorrectLetters.size + 1) / 2)
                    lettersToDisable.forEach { disableLetterButton(it) }
                    remainingTurns--
                    wrongGuesses = 6 - remainingTurns
                    updateHangmanImage()
                    Toast.makeText(this, "Hint: Disabled some incorrect letters", Toast.LENGTH_SHORT).show()
                    hintClickCount++
                    checkGameOver()
                } else {
                    Toast.makeText(this, "No letters to disable", Toast.LENGTH_SHORT).show()
                }
            }
            2 -> {
                if (remainingTurns <= 1) {
                    Toast.makeText(this, "Hint not available", Toast.LENGTH_SHORT).show()
                    return
                }

                val vowels = listOf('A', 'E', 'I', 'O', 'U')
                val vowelsInWord = vowels.filter { it in wordToGuess && it !in guessedLetters }

                if (vowelsInWord.isNotEmpty()) {
                    vowelsInWord.forEach {
                        guessedLetters.add(it)
                        disableLetterButton(it)
                    }
                    updateWordDisplay()
                    vowels.forEach { disableLetterButton(it) }
                    remainingTurns--
                    wrongGuesses = 6 - remainingTurns
                    updateHangmanImage()
                    Toast.makeText(this, "Hint: Revealed all vowels", Toast.LENGTH_SHORT).show()
                    hintClickCount++
                    checkGameOver()
                } else {
                    Toast.makeText(this, "No unrevealed vowels", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(this, "No more hints available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun disableLetterButton(letter: Char) {
        for (i in 0 until letterButtons.childCount) {
            val button = letterButtons.getChildAt(i) as? Button
            if (button?.text.toString().firstOrNull() == letter) {
                button?.isEnabled = false
                break
            }
        }
    }

    /*** Updated methods for saving and restoring state ***/

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("WORD_TO_GUESS", wordToGuess)
        outState.putStringArrayList("GUESSED_LETTERS", ArrayList(guessedLetters.map { it.toString() }))
        outState.putInt("REMAINING_TURNS", remainingTurns)
        outState.putInt("WRONG_GUESSES", wrongGuesses)
        outState.putInt("HINT_CLICK_COUNT", hintClickCount)
        outState.putString("WORD_DISPLAY_TEXT", wordDisplay.text.toString())
        outState.putString("HINT_TEXT", hintTextView?.text.toString())

        // Save disabled letter buttons
        val disabledLetters = mutableListOf<String>()
        for (i in 0 until letterButtons.childCount) {
            val button = letterButtons.getChildAt(i) as? Button
            if (button != null && !button.isEnabled) {
                disabledLetters.add(button.text.toString())
            }
        }
        outState.putStringArrayList("DISABLED_LETTERS", ArrayList(disabledLetters))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        restoreGameState(savedInstanceState)
    }

    private fun restoreGameState(savedInstanceState: Bundle) {
        wordToGuess = savedInstanceState.getString("WORD_TO_GUESS", "").uppercase()
        val guessedLettersList = savedInstanceState.getStringArrayList("GUESSED_LETTERS") ?: arrayListOf()
        guessedLetters = guessedLettersList.map { it.first() }.toMutableSet()
        remainingTurns = savedInstanceState.getInt("REMAINING_TURNS", 6)
        wrongGuesses = savedInstanceState.getInt("WRONG_GUESSES", 0)
        hintClickCount = savedInstanceState.getInt("HINT_CLICK_COUNT", 0)
        wordDisplay.text = savedInstanceState.getString("WORD_DISPLAY_TEXT", "")
        hintTextView?.text = savedInstanceState.getString("HINT_TEXT", "")

        // Restore disabled letter buttons
        val disabledLettersList = savedInstanceState.getStringArrayList("DISABLED_LETTERS") ?: arrayListOf()
        for (i in 0 until letterButtons.childCount) {
            val button = letterButtons.getChildAt(i) as? Button
            if (button != null) {
                button.isEnabled = button.text.toString() !in disabledLettersList
            }
        }

        updateHangmanImage()
        updateWordDisplay()
    }
}
