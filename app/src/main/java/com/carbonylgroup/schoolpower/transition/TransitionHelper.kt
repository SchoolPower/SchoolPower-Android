package com.carbonylgroup.schoolpower.transition

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.transition.Transition
import android.view.View
import android.view.ViewTreeObserver
import com.carbonylgroup.schoolpower.activities.BaseActivity
import java.util.*

/**
 * Provides extra lifecycle events and shims for shared element transitions
 * See the included MainActivity and BaseFragment for example use
 */
class TransitionHelper private constructor(internal val activity: Activity, savedInstanceState: Bundle?) {

    var isAfterEnter = false
        private set

    init {
        isAfterEnter = savedInstanceState != null //if saved instance is not null we've already "entered"
        postponeEnterTransition() //we postpone to prevent status and nav bars from flashing during shared element transitions
    }

    /**
     * Should be called from Activity.onResume()
     */
    fun onResume() {
        if (isAfterEnter) return

        if (!isViewCreatedAlreadyCalled) onViewCreated()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            onAfterEnter()
        } else {
            activity.window.sharedElementEnterTransition.addListener(object : Transition.TransitionListener {
                override fun onTransitionStart(transition: Transition) {
                    if (isAfterEnter) for (listener in listeners) listener.onBeforeReturn()
                }

                override fun onTransitionEnd(transition: Transition) {
                    if (!isAfterEnter) onAfterEnter()
                }

                override fun onTransitionCancel(transition: Transition) {
                    if (!isAfterEnter) onAfterEnter()
                }

                override fun onTransitionPause(transition: Transition) {}

                override fun onTransitionResume(transition: Transition) {}
            })
        }
    }

    /**
     * Should be called from Activity.onBackPressed()
     */
    fun onBackPressed() {
        var isConsumed = false
        for (listener in listeners) {
            isConsumed = listener.onBeforeBack() || isConsumed
        }
        if (!isConsumed) ActivityCompat.finishAfterTransition(activity)
    }

    /**
     * Should be called immediately after all shared transition views are inflated.
     * If using fragments, recommend calling at the beginning of Fragment.onViewCreated().
     */
    private var isViewCreatedAlreadyCalled = false

    fun onViewCreated() {
        if (isViewCreatedAlreadyCalled) return
        isViewCreatedAlreadyCalled = true

        val contentView = activity.window.decorView.findViewById<View>(android.R.id.content)
        for (listener in listeners) listener.onBeforeViewShows(contentView)
        if (!isAfterEnter) {
            for (listener in listeners) listener.onBeforeEnter(contentView)
        }

        if (isPostponeEnterTransition) startPostponedEnterTransition()
    }

    /**
     * Call from Activity.onSaveInstanceState()
     * @param outState
     */
    fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("isAfterEnter", isAfterEnter)
    }

    /**
     * A parent object that owns an instance of TransitionHelper
     * Your Activity should implement Source and call TransitionHelper.init() from Activity.onCreate()
     */
    interface Source {
        var transitionHelper: TransitionHelper?
    }


    /**
     * Listens for extra transition events
     * Activities, Fragments, and other views should implement Listener and call TransitionHelper.of(...).addListener(this)
     */
    interface Listener {
        /**
         * Called during every onViewCreated
         * @param contentView
         */
        fun onBeforeViewShows(contentView: View)

        /**
         * Called during onViewCreated only on an enter transition
         * @param contentView
         */
        fun onBeforeEnter(contentView: View)

        /**
         * Called after enter transition is finished for L+, otherwise called immediately during first onResume
         */
        fun onAfterEnter()


        /**
         * Called during Activity.onBackPressed()
         * @return true if the listener has consumed the event, false otherwise
         */
        fun onBeforeBack(): Boolean

        fun onBeforeReturn()
    }

    private val listeners = ArrayList<Listener>()
    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    private fun onAfterEnter() {
        for (listener in listeners) listener.onAfterEnter()
        isAfterEnter = true
    }

    private var isPostponeEnterTransition = false
    private fun postponeEnterTransition() {
        if (isAfterEnter) return
        ActivityCompat.postponeEnterTransition(activity)
        isPostponeEnterTransition = true
    }


    private fun startPostponedEnterTransition() {
        val decor = activity.window.decorView
        decor.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                decor.viewTreeObserver.removeOnPreDrawListener(this)
                ActivityCompat.startPostponedEnterTransition(activity)
                return true
            }
        })
    }

    open class MainActivity : BaseActivity(), TransitionHelper.Source, TransitionHelper.Listener {
        override var transitionHelper: TransitionHelper? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            TransitionHelper.init(this, savedInstanceState)
            TransitionHelper.of(this).addListener(this)
            super.onCreate(savedInstanceState)
        }

        public override fun onSaveInstanceState(outState: Bundle) {
            TransitionHelper.of(this).onSaveInstanceState(outState)
            super.onSaveInstanceState(outState)
        }

        override fun onResume() {
            TransitionHelper.of(this).onResume()
            super.onResume()
        }

        override fun onBackPressed() = TransitionHelper.of(this).onBackPressed()

        override fun onBeforeViewShows(contentView: View) {}

        override fun onBeforeEnter(contentView: View) {}

        override fun onAfterEnter() {}

        override fun onBeforeBack() = false

        override fun onBeforeReturn() {}
    }

    open class BaseFragment : Fragment(), TransitionHelper.Listener {

        override fun onCreate(savedInstanceState: Bundle?) {
            TransitionHelper.of(activity as MainActivity).addListener(this)
            super.onCreate(savedInstanceState)

        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            TransitionHelper.of(activity as MainActivity).onViewCreated()
            super.onViewCreated(view, savedInstanceState)
        }

        override fun onBeforeViewShows(contentView: View) {

        }

        override fun onBeforeEnter(contentView: View) {

        }

        override fun onAfterEnter() {

        }

        override fun onBeforeBack(): Boolean {
            return false
        }

        override fun onBeforeReturn() {

        }
    }

    companion object {
        //STATICS:
        /**
         * Get the TransitionHelper object for an Activity
         * @param a
         * *
         * @return
         */
        fun of(a: Activity): TransitionHelper {
            return (a as Source).transitionHelper!!
        }

        /**
         * Initialize the TransitionHelper object.  Should be called at the beginning of Activity.onCreate()
         * @param source
         * *
         * @param savedInstanceState
         */
        fun init(source: Source, savedInstanceState: Bundle?) {
            source.transitionHelper = TransitionHelper(source as Activity, savedInstanceState)
        }
    }


}
