package com.ta.tambahinaja.utils

import com.ta.tambahinaja.play.GameType
import com.ta.tambahinaja.play.practice.PracticeGamePlay

fun setGamePlay(level: Int) : PracticeGamePlay{
    var gamePlay = PracticeGamePlay()
    when(level){
        1 -> gamePlay = PracticeGamePlay(GameType.Normal, 3, 0,3, 10, 3, 50,false)
        2 -> gamePlay = PracticeGamePlay(GameType.Normal, 5, 0,3, 10, 3, 50,false)
        3 -> gamePlay = PracticeGamePlay(GameType.Normal, 5, 0,4, 8, 3, 50,true,30)
        4 -> gamePlay = PracticeGamePlay(GameType.Normal, 8, 0,4, 10, 3, 50,true,30)
        5 -> gamePlay = PracticeGamePlay(GameType.OddEven, 5, 0,4, 10, 3, 50,true,30)
        6 -> gamePlay = PracticeGamePlay(GameType.OddEven, 8, 0,3, 10, 3, 50,true,30)
        7 -> gamePlay = PracticeGamePlay(GameType.Normal, 9, 0,3, 10, 3, 50,true,30)
        8 -> gamePlay = PracticeGamePlay(GameType.Normal, 9, 0,3, 10, 3, 50,true,30)
        9 -> gamePlay = PracticeGamePlay(GameType.Normal, 9, 0,3, 10, 3, 50,true,30)
        10 -> gamePlay = PracticeGamePlay(GameType.Rush, 3, 0,3, 10, 3, 50,true,30)
        11 -> gamePlay = PracticeGamePlay(GameType.Rush, 5, 0,3, 10, 3, 50,true,30)
        12 -> gamePlay = PracticeGamePlay(GameType.Rush, 8, 0,3, 10, 3, 50,true,30)
        13 -> gamePlay = PracticeGamePlay(GameType.Rush, 9, 0,3, 10, 3, 50,true,30)
        14 -> gamePlay = PracticeGamePlay(GameType.Rush, 9, 0,3, 10, 3, 50,true,30)
        15 -> gamePlay = PracticeGamePlay(GameType.AlphaNum, 3, 2,3, 10, 3, 50,false,30)
        16 -> gamePlay = PracticeGamePlay(GameType.AlphaNum, 5, 3,3, 10, 3, 50,true,30)
        17 -> gamePlay = PracticeGamePlay(GameType.AlphaNum, 8, 5,3, 10, 3, 50,true,30)
        18 -> gamePlay = PracticeGamePlay(GameType.AlphaNum, 9, 8,3, 10, 3, 50,true,30)
        19 -> gamePlay = PracticeGamePlay(GameType.AlphaNum, 9, 8,3, 10, 3, 50,true,30)
        20 -> gamePlay = PracticeGamePlay(GameType.Mix, 9, 9,3, 10, 3, 50,true,30)
        21 -> gamePlay = PracticeGamePlay(GameType.Mix, 9, 9,3, 10, 3, 50,true,30)
        22 -> gamePlay = PracticeGamePlay(GameType.Mix, 9, 9,3, 10, 3, 50,true,30)
        23 -> gamePlay = PracticeGamePlay(GameType.OddEven, 9, 0,3, 10, 3, 50,true,30)
        24 -> gamePlay = PracticeGamePlay(GameType.OddEven, 9, 0,3, 10, 3, 50,true,30)
        25 -> gamePlay = PracticeGamePlay(GameType.OddEven, 9, 0,3, 10, 3, 50,true,30)
        26 -> gamePlay = PracticeGamePlay(GameType.Normal, 9, 0,3, 10, 3, 50,true,30)
        27 -> gamePlay = PracticeGamePlay(GameType.Normal, 9, 0,3, 10, 3, 50,true,30)
        28 -> gamePlay = PracticeGamePlay(GameType.Rush, 9, 0,3, 10, 3, 50,true,30)
        29 -> gamePlay = PracticeGamePlay(GameType.Normal, 9, 0,3, 10, 3, 50,true,30)
        30 -> gamePlay = PracticeGamePlay(GameType.Normal, 9, 0,3, 10, 3, 50,true,30)
    }
    return gamePlay
}