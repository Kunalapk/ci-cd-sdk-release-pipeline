package com.kunalapk.cicdpipeline

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kunalapk.cicdpipeline.ui.theme.CicdpipelineTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CicdpipelineTheme {

            }
        }
    }
}