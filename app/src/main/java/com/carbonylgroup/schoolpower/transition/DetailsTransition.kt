/**
 * Copyright (C) 2016 Gustav Wang
 */

package com.carbonylgroup.schoolpower.transition

import android.transition.ChangeBounds
import android.transition.ChangeImageTransform
import android.transition.ChangeTransform
import android.transition.TransitionSet


class DetailsTransition : TransitionSet() {
    init {
        ordering = TransitionSet.ORDERING_TOGETHER
        addTransition(ChangeBounds()).addTransition(ChangeTransform())
        addTransition(ChangeImageTransform())
    }

}
