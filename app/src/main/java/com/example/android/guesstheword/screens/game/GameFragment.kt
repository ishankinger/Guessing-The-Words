/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.guesstheword.screens.game

import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment.findNavController
import com.example.android.guesstheword.R
import com.example.android.guesstheword.databinding.GameFragmentBinding


class GameFragment : Fragment() {

    // first make a field for game view model
    private lateinit var viewModel : GameViewModel

    // can't move this to GameViewModel as it contains references to all the views
    private lateinit var binding: GameFragmentBinding

// _____________Moved to ViewModel_______________________________________________
//    // The current word
//    private var word = ""
//
//    // The current score
//    private var score = 0
//
//    // The list of words - the front of the list is the next word to guess
//    private lateinit var wordList: MutableList<String>
//    ___________________________________________________________________________

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Inflate view and obtain an instance of the binding class
        binding = DataBindingUtil.inflate(
                inflater,
                R.layout.game_fragment,
                container,
                false
        )

        // association of gameViewModel and gameFragment using Providers function
        Log.i("GameFragment","Called ViewModelProviders.of")
        viewModel = ViewModelProviders.of(this).get(GameViewModel::class.java)

//        _______________Moved to init block________________________
//        resetList()
//        nextWord()
//        _____________________________________________________________

        // in onClickListener we are using the updateScoreText and updateWordText() functions
        // thus we can remove the calls of these from nextWord which is moved to view Model
//        binding.correctButton.setOnClickListener {
//            viewModel.onCorrect()
//
//            // the first ambiguity of using these function calls is removed now by using live data
//            // calling viewModel.observe(______)
//            // updateScoreText()
//            // updateWordText()
//        }
//        binding.skipButton.setOnClickListener{
//            viewModel.onSkip()
//            // updateScoreText()
//            // updateWordText()
//        }

        // now instead of using the above two onClickListeners we connect ViewModel and Views of XML directly
        // now we can directly write the above codes in the xml of game fragment
        binding.gameViewModel = viewModel


//        // using live data observer method to update the live data directly without using any methods
//        viewModel.score.observe(viewLifecycleOwner, Observer{ newScore->
//            binding.scoreText.text = newScore.toString()
//        })
//        viewModel.word.observe(viewLifecycleOwner,Observer{newWord ->
//            binding.wordText.text = newWord
//        })

        // now instead of using the above two observer methods we can directly change the value of
        // views using live data as
        binding.setLifecycleOwner(this)

        viewModel.eventGameFinish.observe(viewLifecycleOwner,Observer{hasFinished->
            if(hasFinished){
                gameFinished()
                viewModel.onGameFinishComplete()
            }
        })

        viewModel.buzzer.observe(viewLifecycleOwner,Observer{buzzedType->
            if(!buzzedType.pattern.contentEquals(longArrayOf(0))){
                buzz(buzzedType.pattern)
                viewModel.onBuzzComplete()
            }
        })

        // as complex operations are performed in this thus some additional code is written in model
//        viewModel.currentTime.observe(viewLifecycleOwner,Observer{newTime->
//            var newTimeString = (newTime/1000).toString()
//            newTimeString = "00:$newTimeString"
//            binding.timerText.text = newTimeString
//        })

        //updateScoreText()
        //updateWordText()

        return binding.root

    }

//    // function run when the activity is started
//    override fun onStart(){
//        super.onStart()
//        Log.i("MainActivity","onStart Called")
//        // to start timer when onStart is called
//        viewModel.eventPauseTime.observe(viewLifecycleOwner,Observer{hasPaused->
//            if(hasPaused){
//                viewModel.onTimeStart()
//            }
//
//        })
//    }
//
//    // function run when the activity is stopped
//    override fun onStop(){
//        super.onStop()
//        Log.i("MainActivity","onStop Called")
//        // to stop timer when onStop is called
//        viewModel.eventPauseTime.observe(viewLifecycleOwner,Observer{hasPaused->
//            if(hasPaused){
//                viewModel.onTimePause()
//            }
//
//        })
//    }

//    ______________________moved to model______________________________________________
//    /**
//     * Resets the list of words and randomizes the order
//     */
//    private fun resetList() {
//        wordList = mutableListOf(
//                "queen",
//                "hospital",
//                "basketball",
//                "cat",
//                "change",
//                "snail",
//                "soup",
//                "calendar",
//                "sad",
//                "desk",
//                "guitar",
//                "home",
//                "railway",
//                "zebra",
//                "jelly",
//                "car",
//                "crow",
//                "trade",
//                "bag",
//                "roll",
//                "bubble"
//        )
//        wordList.shuffle()
//    }
//     * Moves to the next word in the list
//     */
//    private fun nextWord() {
//        //Select and remove a word from the list
//        if (wordList.isEmpty()) {
//            gameFinished()
//        } else {
//            word = wordList.removeAt(0)
//        }
//        updateWordText()
//        updateScoreText()
//    }
//    _________________________________________________________________________________


    // Called when the game is finished
    // uses navigation so can't moved to game view model
    private fun gameFinished() {
        //__________________________________________________________________________________________
        // val action = GameFragmentDirections.actionGameToScore(viewModel.score)
        // as now we are using score as live data so we have to change call for score from model also
        // we are here using elvis operator(if integer value then pass else 0 passed)
        val action = GameFragmentDirections.actionGameToScore(viewModel.score.value ?: 0)
        //__________________________________________________________________________________________

        findNavController(this).navigate(action)
        Toast.makeText(this.activity,"Game has finished", Toast.LENGTH_SHORT).show()
    }

    private fun buzz(pattern: LongArray) {
        val buzzer = activity?.getSystemService<Vibrator>()

        buzzer?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                buzzer.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                //deprecated in API 26
                buzzer.vibrate(pattern, -1)
            }
        }
    }


//    _____________Moved to model and removing private keyword____________________________
//    // Methods for buttons presses
//    private fun onSkip() {
//        score--
//        nextWord()
//    }
//
//    private fun onCorrect() {
//        score++
//        nextWord()
//    }
//    ________________________________________________________________________________________



    // Methods for updating the UI
    // in these methods word and score are used as viewModel.(_____)
    // now these methods are of not use as we are directly using these methods in live data observer
//    _____________________________________________________________________________________________
//    private fun updateWordText() {
//        binding.wordText.text = viewModel.word
//
//    }

//    private fun updateScoreText() {
//        binding.scoreText.text = viewModel.score.toString()
//    }
// ________________________________________________________________________________________________
}
