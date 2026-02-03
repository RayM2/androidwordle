package com.example.wordle

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible

class MainActivity : AppCompatActivity() {

    private lateinit var wordToGuess: String
    private var guessCount = 0
    private val guessTextViews by lazy {
        listOf(
            findViewById<TextView>(R.id.guess1TextView),
            findViewById<TextView>(R.id.guess2TextView),
            findViewById<TextView>(R.id.guess3TextView)
        )
    }
    private val guessCheckTextViews by lazy {
        listOf(
            findViewById<TextView>(R.id.guess1CheckTextView),
            findViewById<TextView>(R.id.guess2CheckTextView),
            findViewById<TextView>(R.id.guess3CheckTextView)
        )
    }
    private val guessEditText by lazy { findViewById<EditText>(R.id.guessEditText) }
    private val submitButton by lazy { findViewById<Button>(R.id.submitButton) }
    private val wordToGuessTextView by lazy { findViewById<TextView>(R.id.wordToGuessTextView) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        wordToGuess = FourLetterWordList.getRandomFourLetterWord()
        // Use this for debugging if needed
        // android.util.Log.d("MainActivity", "Word to guess: $wordToGuess")

        // Hide the old check TextViews as they are no longer needed
        guessCheckTextViews.forEach { it.isVisible = false }

        submitButton.setOnClickListener {
            val guess = guessEditText.text.toString().uppercase()
            if (guess.length != 4) {
                Toast.makeText(this, "Please enter a 4-letter word", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (guessCount < 3) {
                val result = checkGuess(guess)
                val spannableGuess = getSpannable(guess, result)
                guessTextViews[guessCount].text = spannableGuess
                guessCount++
                guessEditText.text.clear()

                if (result == "OOOO") {
                    Toast.makeText(this, "Congratulations! You guessed the word!", Toast.LENGTH_SHORT).show()
                    endGame()
                } else if (guessCount == 3) {
                    endGame()
                }
            }
        }
    }

    private fun getSpannable(guess: String, result: String): SpannableString {
        val spannable = SpannableString(guess)
        for (i in 0..3) {
            val color = when (result[i]) {
                'O' -> Color.GREEN
                '+' -> Color.YELLOW
                else -> Color.RED
            }
            spannable.setSpan(ForegroundColorSpan(color), i, i + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        return spannable
    }

    private fun endGame() {
        wordToGuessTextView.text = "The word was: $wordToGuess"
        submitButton.isEnabled = false
    }

    private fun checkGuess(guess: String): String {
        val result = CharArray(4)
        val wordToGuessLetterCounts = wordToGuess.groupingBy { it }.eachCount().toMutableMap()

        // 1st pass: Find correct letters in the correct position (O)
        for (i in 0..3) {
            if (guess[i] == wordToGuess[i]) {
                result[i] = 'O'
                wordToGuessLetterCounts[guess[i]] = wordToGuessLetterCounts.getOrDefault(guess[i], 0) - 1
            }
        }

        // 2nd pass: Find misplaced (+) and incorrect (X) letters
        for (i in 0..3) {
            if (result[i] == 'O') continue

            val letterCount = wordToGuessLetterCounts.getOrDefault(guess[i], 0)
            if (letterCount > 0) {
                result[i] = '+'
                wordToGuessLetterCounts[guess[i]] = letterCount - 1
            } else {
                result[i] = 'X'
            }
        }

        return String(result)
    }
}
