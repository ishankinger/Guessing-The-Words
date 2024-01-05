package com.example.android.guesstheword.screens.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

private val CORRECT_BUZZ_PATTERN = longArrayOf(100, 100, 100, 100)
private val PANIC_BUZZ_PATTERN = longArrayOf(0, 200)
private val GAME_OVER_BUZZ_PATTERN = longArrayOf(0, 200)
private val NO_BUZZ_PATTERN = longArrayOf(0)

enum class BuzzType(val pattern: LongArray) {
    CORRECT(CORRECT_BUZZ_PATTERN),
    GAME_OVER(GAME_OVER_BUZZ_PATTERN),
    COUNTDOWN_PANIC(PANIC_BUZZ_PATTERN),
    NO_BUZZ(NO_BUZZ_PATTERN),
}

class GameViewModel : ViewModel() {

    // The current score
    // >>> make score non private as it is used in gameFragment
    // var score = 0
    // >>> wrap the variable with liveData
    // var score = MutableLiveData<Int>()
    // >>> using encapsulation
    private val _score = MutableLiveData<Int>()
    val score : LiveData<Int>
            get() = _score

    // The current word
    // make word non private as it is also used in gameFragment
    // var word = ""
    // var word = MutableLiveData<String>()
    private val _word = MutableLiveData<String>()
    val word : LiveData<String>
            get() = _word

    // The list of words - the front of the list is the next word to guess
    private lateinit var wordList: MutableList<String>

    // live data of boolean type for game finish
    private val _eventGameFinish = MutableLiveData<Boolean>()
    val eventGameFinish : LiveData<Boolean>
            get() = _eventGameFinish

    // defining variable for counting time
    private val timer : CountDownTimer

    // encapsulated live data for current time
    private val _currentTime = MutableLiveData<Long>()
    val currentTime : LiveData<Long>
            get() = _currentTime

    // To prevent use of UI controller as a relay
    val currentTimeString = Transformations.map(currentTime) { time ->
        "00:"+(time/1000).toString()
    }

    // an encapsulated LiveData for the buzz
    private val _buzzer = MutableLiveData<BuzzType>()
    val buzzer : LiveData<BuzzType>
            get() = _buzzer

    companion object {
        // These represent different important times
        // This is when the game is over
        private const val DONE = 0L
        // This is the number of milliseconds in a second
        private const val ONE_SECOND = 1000L
        // This is the total time of the game
        private const val COUNTDOWN_TIME = 60000L
        // This is the panic time after which continuous buzzing occurs
        private const val COUNTDOWN_PANIC_TIME = 10000L
    }

    // initialised block when view model created it jumps into it
    init{
        // log statement to check when this init block is called
        Log.i("GameViewModel","GameViewModel created")

        // initialising the values for live data variables
        _score.value = 0
        _word.value = ""
        _eventGameFinish.value = false
        _currentTime.value = COUNTDOWN_TIME

        // function calls move to init so that when viewModel is created they are called
        resetList()
        nextWord()

        // for timer code---
        timer = object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {

            // function called for every tick of time
            override fun onTick(millisUntilFinished: Long) {
                _currentTime.value = (_currentTime).value?.minus(ONE_SECOND)
                if(_currentTime.value == DONE){
                    onFinish()
                }
                // to start panic buzzer
                if(_currentTime.value!! <= COUNTDOWN_PANIC_TIME){
                    _buzzer.value = BuzzType.COUNTDOWN_PANIC
                }
            }

            // function called to finish the time
            override fun onFinish() {
                _currentTime.value = DONE
                _eventGameFinish.value = true
                // to start gameOver buzzer
                _buzzer.value = BuzzType.GAME_OVER
            }
        }
        timer.start()

    }

    // Resets the list of words and randomizes the order
    private fun resetList() {
        wordList = mutableListOf(
            "queen",
            "hospital",
            "basketball",
            "cat",
            "change",
            "snail",
            "soup",
            "calendar",
            "sad",
            "desk",
            "guitar",
            "home",
            "railway",
            "zebra",
            "jelly",
            "car",
            "crow",
            "trade",
            "bag",
            "roll",
            "bubble"
        )
        wordList.shuffle()
    }

    // Moves to the next word in the list
    private fun nextWord() {
        //Select and remove a word from the list
        if (wordList.isEmpty()) {
            // >>> can't use this method here
            // gameFinished()
            // >>> making the eventGameFinish variable to be true
            //_eventGameFinish.value = true
            // >>> now we are adding timer so we donot want to finish the game when words finishes
            resetList()
        }
//        else {
//            _word.value = wordList.removeAt(0)
//    }
        _word.value = wordList.removeAt(0)
    }

    // Methods for buttons presses
    // remove the private keyword as these methods have dual nature
    // as updating the score and showing it also.
    fun onSkip() {
        //score--
        // make it according to live data
        _score.value = (score.value)?.minus(1)
        nextWord()
    }
    fun onCorrect() {
        _score.value = (score.value)?.plus(1)
        // to start buzzer on correct
        _buzzer.value = BuzzType.CORRECT
        nextWord()
    }

    // to prevent one small bug that is after changing configuration each time game finished
    // method will be called
    fun onGameFinishComplete(){
        _eventGameFinish.value = false
    }

    fun onBuzzComplete(){
        _buzzer.value = BuzzType.NO_BUZZ
    }

    // on cleared function called when navigate to any other fragment thus destroying fragment
    // associated with game view model
    override fun onCleared(){
        super.onCleared()
        Log.i("GameViewModel","GameViewModel destroyed")

        // to avoid memory leaks always cancel a countDownTimer if no longer need it
        timer.cancel()
    }
}