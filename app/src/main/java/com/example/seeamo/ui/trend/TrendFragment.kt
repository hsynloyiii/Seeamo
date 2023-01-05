package com.example.seeamo.ui.trend

import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import com.example.seeamo.utilize.base.BaseFragment
import com.example.seeamo.utilize.extensions.defaultAppearance
import com.example.seeamo.utilize.helper.LayoutHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrendFragment: BaseFragment(false) {

    private lateinit var mainLayout: NestedScrollView
    private lateinit var textView: TextView
    override fun createViews(savedInstanceState: Bundle?) {
        root = NestedScrollView(requireContext())
        mainLayout = (root as NestedScrollView).apply {
            defaultAppearance(baseColor)
        }

        textView = TextView(context).apply {
            text = "jidsc sdijcidjsip jdffsvfbvfbdbdfbfbbfsbsffksj oaeoe oaew oajidsc sdijcidjsip jdffsvfbvfbdbdfbfbbfsbsffksj oaeoe oaew oajidsc sdijcidjsip jdffsvfbvfbdbdfbfbbfsbsffksj oaeoe oaew oajidsc sdijcidjsip jdffsvfbvfbdbdfbfbbfsbsffksj oaeoe oaew oajidsc sdijcidjsip jdffsvfbvfbdbdfbfbbfsbsffksj oaeoe oaew oajidsc sdijcidjsip jdffsvfbvfbdbdfbfbbfsbsffksj oaeoe oaew oajidsc sdijcidjsip jdffsvfbvfbdbdfbfbbfsbsffksj oaeoe oaew oajidsc sdijcidjsip jdffsvfbvfbdbdfbfbbfsbsffksj oaeoe oaew oajidsc sdijcidjsip jdffsvfbvfbdbdfbfbbfsbsffksj oaeoe oaew oajidsc sdijcidjsip jdffsvfbvfbdbdfbfbbfsbsffksj oaeoe oaew oajidsc sdijcidjsip jdffsvfbvfbdbdfbfbbfsbsffksj oaeoe oaew oajidsc sdijcidjsip jdffsvfbvfbdbdfbfbbfsbsffksj oaeoe oaew oajidsc sdijcidjsip jdffsvfbvfbdbdfbfbbfsbsffksj oaeoe oaew oajidsc sdijcidjsip jdffsvfbvfbdbdfbfbbfsbsffksj oaeoe oaew oajidsc sdijcidjsip jdffsvfbvfbdbdfbfbbfsbsffksj oaeoe oaew oajidsc sdijcidjsip jdffsvfbvfbdbdfbfbbfsbsffksj oaeoe oaew oajidsc sdijcidjsip jdffsvfbvfbdbdfbfbbfsbsffksj oaeoe oaew oajidsc sdijcidjsip jdffsvfbvfbdbdfbfbbfsbsffksj oaeoe oaew oajidsc sdijcidjsip jdffsvfbvfbdbdfbfbbfsbsffksj oaeoe oaew oajidsc sdijcidjsip jdffsvfbvfbdbdfbfbbfsbsffksj oaeoe oaew oawjopf ja jfpoew jfopwji cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh iojidsc sdijcidjsip jdi cidosj iodh ish oihdsoihcdso ihio hiosdhcisdoh idsh iodsh io"
            mainLayout.addView(
                this,
                LayoutHelper.MATCH_PARENT,
                LayoutHelper.MATCH_PARENT
            )
        }
    }

    override fun setup(savedInstanceState: Bundle?) {

    }
}