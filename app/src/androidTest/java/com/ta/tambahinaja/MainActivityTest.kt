package com.ta.tambahinaja

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.android.material.bottomnavigation.BottomNavigationMenu
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

import com.ta.tambahinaja.main.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Rule
    @JvmField var activityRule = ActivityTestRule(MainActivity::class.java)
//    lateinit var bottomNavigation : BottomNavigationView
//    lateinit var menuStringContent
//
//    @Before
//    fun setUp(){
//        val activity = activityRule.activity
//        bottomNavigation = activity.findViewById(R.id.bottom_navigation)
//
//        val res = activity.resources
//        mMenuStringContent = new HashMap<>(MENU_CONTENT_ITEM_IDS.length);
//        mMenuStringContent.put(R.id.destination_home, res.getString(R.string.navigate_home));
//        mMenuStringContent.put(R.id.destination_profile, res.getString(R.string.navigate_profile));
//        mMenuStringContent.put(R.id.destination_people, res.getString(R.string.navigate_people));
//    }

    @Test
    fun testBottomNavigation() {

        onView(withId(R.id.bottom_navigation))
                .check(matches(isDisplayed()))
        onView(withId(R.id.tournament)).perform(click())

        onView(withId(R.id.bottom_navigation))
                .check(matches(isDisplayed()))
        onView(withId(R.id.profile)).perform(click())

        onView(withId(R.id.bottom_navigation))
                .check(matches(isDisplayed()))
        onView(withId(R.id.home)).perform(click())
    }

    @Test
    fun testAppBehaviour() {

        onView(withId(R.id.btnRank))
                .check(matches(isDisplayed()))
        onView(withId(R.id.btnRank)).perform(click())

        onView(withId(R.id.btnMessageClose))
                .check(matches(isDisplayed()))
        onView(withId(R.id.btnMessageClose)).perform(click())

        onView(withId(R.id.btnCustomPlay))
                .check(matches(isDisplayed()))
        onView(withId(R.id.btnCustomPlay)).perform(click())

        onView(withId(R.id.btnClose))
                .check(matches(isDisplayed()))
        onView(withId(R.id.btnClose)).perform(click())

        onView(withId(R.id.btnLeaderboard))
                .check(matches(isDisplayed()))
        onView(withId(R.id.btnLeaderboard)).perform(click())

        pressBack()

        onView(withId(R.id.btnOnline))
                .check(matches(isDisplayed()))
        onView(withId(R.id.btnOnline)).perform(click())

        pressBack()

        onView(withId(R.id.ivShop))
                .check(matches(isDisplayed()))
        onView(withId(R.id.ivShop)).perform(click())

        pressBack()

        onView(withId(R.id.cvCredit))
                .check(matches(isDisplayed()))
        onView(withId(R.id.cvCredit)).perform(click())

        pressBack()
    }

}