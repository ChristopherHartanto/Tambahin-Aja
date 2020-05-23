//package com.ta.tambahinaja.utils
//
//class SimpleCountingIdlingResource : IdlingResource {
//    private var callback: IdlingResource.ResourceCallback? = null
//
//    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
//        this.callback = callback
//    }
//
//    override fun getName(): String {
//        return "${SimpleCountingIdlingResource::class.java}"
//    }
//
//    override fun isIdleNow(): Boolean {
//        val idle = MainPresenter.idlingResourceCounter == 0
//        if(idle){
//            callback?.onTransitionToIdle()
//        }
//        return  idle
//    }
//
//}