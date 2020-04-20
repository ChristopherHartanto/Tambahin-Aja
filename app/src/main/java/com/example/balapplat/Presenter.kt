//package com.example.balapplat
//
//import android.provider.Contacts
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//import org.jetbrains.anko.coroutines.experimental.bg
//
//class Presenter(private val view: MainView,
//                    private val gson: Gson, private val context: CoroutineContextProvider = CoroutineContextProvider()) {
//
//    companion object {
//        var idlingResourceCounter = 1
//    }
//
//    fun getEventList(idEvent : String?) {
//        view.showLoading()
//
//        GlobalScope.launch(context.main){
//            val data = bg{
//                gson.fromJson(apiRepository
//                    .doRequest(TheSportDBApi.LastMatch(idEvent)),
//                    EventResponse::class.java)
//            }
//            view.showHighScore(data.await().events)
//            view.hideLoading()
//            idlingResourceCounter = 0
//
//        }
//    }
//
//
//}