package com.example.LostHeart

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.util.*

class MainActivity : Activity() {

    private lateinit var highestScoreTextView: TextView
    private var highestScore = 0
    private lateinit var sharedPreferences: SharedPreferences
    private var isGameEnded = false // Declare boolean flag to track game end

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)
        highestScore = sharedPreferences.getInt("highestScore", 0)

        val resetButton = findViewById<Button>(R.id.reset_highest_score)
        highestScoreTextView = findViewById(R.id.highest_score)
        highestScoreTextView.text = "Highest Score: $highestScore"

        val board = findViewById<RelativeLayout>(R.id.board)
        val upButton = findViewById<Button>(R.id.up)
        val downButton = findViewById<Button>(R.id.down)
        val leftButton = findViewById<Button>(R.id.left)
        val rightButton = findViewById<Button>(R.id.right)
        val pauseButton = findViewById<Button>(R.id.pause)
        val mainMenu = findViewById<Button>(R.id.MainMenu)
        val newgame = findViewById<Button>(R.id.new_game)
        val resume = findViewById<Button>(R.id.resume)
        val playagain = findViewById<RelativeLayout>(R.id.board1)
        val score2 = findViewById<Button>(R.id.score2)
        val endGameButton = findViewById<Button>(R.id.end_game)
        highestScoreTextView = findViewById(R.id.highest_score)
        highestScoreTextView.text = " Highest --- Score: $highestScore"

        val boy = ImageView(this)
        val girl = ImageView(this)
        val badman = ImageView(this) // Add badman ImageView
        val girlSegments = mutableListOf(girl)
        val handler = Handler()
        var delayMillis = 25L
        var currentDirection = "right"
        var scorex = -1

        board.visibility = View.INVISIBLE
        playagain.visibility = View.INVISIBLE
        score2.visibility = View.VISIBLE

        resetButton.setOnClickListener {
            // Reset the highest score to 0
            highestScore = 0
            highestScoreTextView.text = " Highest --- Score: $highestScore"
            saveHighestScore()
        }

        newgame.setOnClickListener {
            board.visibility = View.VISIBLE
            newgame.visibility = View.INVISIBLE
            resume.visibility = View.INVISIBLE
            score2.visibility = View.VISIBLE
            resetButton.visibility = View.INVISIBLE
            endGameButton.visibility = View.VISIBLE

            val girlWidth = 172 // Snake width in pixels
            val girlHeight = 250 // Snake height in pixels
            val boyWidth = 112 // Meat width in pixels
            val boyHeight = 294 // Meat height in pixels
            val badmanWidth = 160 // Badman width in pixels
            val badmanHeight = 240 // Badman height in pixels
            badman.scaleX=-1f

            girl.setImageResource(R.drawable.girl)
            girl.setPadding(10, 10, 10, 10) // Add padding to increase touch-sensitive area
            girl.layoutParams = ViewGroup.LayoutParams(girlWidth, girlHeight)
            board.addView(girl)
            girlSegments.add(girl)

            var girlX = girl.x
            var girlY = girl.y

            boy.setImageResource(R.drawable.boy)
            boy.setPadding(-10, -80, -10, -60) // Add padding to increase touch-sensitive area
            boy.layoutParams = ViewGroup.LayoutParams(boyWidth, boyHeight)
            board.addView(boy)

            badman.setImageResource(R.drawable.badman) // Assuming "badman" is the name of your vector drawable
            badman.layoutParams = ViewGroup.LayoutParams(badmanWidth, badmanHeight)
            board.addView(badman)

            // Function to generate random coordinates for badman within the board bounds
            fun generateRandomPosition(): Pair<Float, Float> {
                //val randomX = Random().nextInt(500 - badmanWidth)
                //val randomY = Random().nextInt(500 - badmanHeight)
                return Pair(400f, 550f)
            }

            // Position the badman at a random location initially
            var (badmanX, badmanY) = generateRandomPosition()
            badman.x = badmanX
            badman.y = badmanY

            // Add logic to position the badman within the board layout

            fun checkFoodCollision() {
                val boyBounds = Rect()
                boy.getHitRect(boyBounds)

                for (segment in girlSegments) {
                    val segmentBounds = Rect()
                    segment.getHitRect(segmentBounds)

                    if (Rect.intersects(boyBounds, segmentBounds)) {
                        val randomX = Random().nextInt(board.width - 200)
                        val randomY = Random().nextInt(board.height - 200)

                        boy.x = randomX.toFloat()
                        boy.y = randomY.toFloat()



                        delayMillis--
                        scorex++
                        score2.text = "score : $scorex"

                        if (!isGameEnded) { // Check the flag before updating the highest score
                            if (scorex > highestScore) {
                                highestScore = scorex
                                highestScoreTextView.text = " Highest --- Score: $highestScore"
                                saveHighestScore()
                            }
                        }

                        break // Exit the loop once collision is detected
                    }
                }
            }

            fun checkBadmanCollision() {
                val badmanBounds = Rect()
                badman.getHitRect(badmanBounds)

                for (segment in girlSegments) {
                    val segmentBounds = Rect()
                    segment.getHitRect(segmentBounds)

                    if (Rect.intersects(badmanBounds, segmentBounds)) {
                        isGameEnded = true // End the game if badman collision detected
                        playagain.visibility = View.VISIBLE
                        board.visibility = View.INVISIBLE
                        newgame.visibility = View.INVISIBLE
                        mainMenu.visibility = View.VISIBLE

                        return // Exit the function once collision is detected
                    }
                }
            }

            // Define a function to move the badman
            fun moveBadman() {
                // Implement your logic to move the badman here
                // For example, you can move it randomly or towards a specific direction
                // Here's a simple example of moving the badman towards the girl's current position
                val dx = (boy.x-10) - badman.x
                val dy = boy.y - badman.y

                // Move badman towards the girl's position
                badman.x += dx / 340
                badman.y += dy / 340
            }

            val badmanMovementHandler = Handler()
            val badmanMovementRunnable = object : Runnable {
                override fun run() {
                    moveBadman()
                    checkBadmanCollision()
                    badmanMovementHandler.postDelayed(this, delayMillis)
                }
            }
            // Start moving the badman
            badmanMovementHandler.postDelayed(badmanMovementRunnable, delayMillis)

            val runnable = object : Runnable {
                override fun run() {
                    for (i in girlSegments.size - 1 downTo 1) {
                        girlSegments[i].x = girlSegments[i - 1].x
                        girlSegments[i].y = girlSegments[i - 1].y
                    }

                    when (currentDirection) {
                        "up" -> {
                            girlY -= 3
                            if (girlY < -600) {
                                girlY = 760f
                            }
                            girl.translationY = girlY
                        }
                        "down" -> {
                            girlY += 3
                            if (girlY > 1020 - girl.height) {
                                girlY = -650f
                            }
                            girl.translationY = girlY
                        }
                        "left" -> {
                            girlX -= 3
                            if (girlX < -500) {
                                girlX = 560f
                            }
                            girl.scaleX = -1f // Flip the girl horizontally
                            girl.translationX = girlX
                        }
                        "right" -> {
                            girlX += 3
                            if (girlX > 600 - girl.width) {
                                girlX = -500f
                            }
                            girl.scaleX = 1f
                            girl.translationX = girlX
                        }
                        "pause" -> {
                            // No need to update position when paused
                        }
                    }

                    checkFoodCollision()
                    checkBadmanCollision() // Check for badman collision
                    handler.postDelayed(this, delayMillis)
                }
            }

            handler.postDelayed(runnable, delayMillis)

            upButton.setOnClickListener {
                currentDirection = "up"
            }
            downButton.setOnClickListener {
                currentDirection = "down"
            }
            leftButton.setOnClickListener {
                currentDirection = "left"
            }
            rightButton.setOnClickListener {
                currentDirection = "right"
            }
            pauseButton.setOnClickListener {
                currentDirection = "pause"
                board.visibility = View.INVISIBLE
                resume.visibility = View.VISIBLE
                endGameButton.visibility = View.VISIBLE
            }
            resume.setOnClickListener {
                currentDirection = "right"
                board.visibility = View.VISIBLE
                resume.visibility = View.INVISIBLE
            }
            endGameButton.setOnClickListener {
                isGameEnded = true // Update the flag when the game ends
                finish()
            }
        }

        mainMenu.setOnClickListener {
            val Intent = Intent(this, newGame::class.java)
            startActivity(Intent)
        }

        hideSystemUI()
    }

    private fun saveHighestScore() {
        val editor = sharedPreferences.edit()
        editor.putInt("highestScore", highestScore)
        editor.apply()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView.findViewById(android.R.id.content)).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
