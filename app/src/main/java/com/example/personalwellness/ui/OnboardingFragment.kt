package com.example.personalwellness.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.personalwellness.R

class OnboardingFragment : Fragment() {

    companion object {
        private const val ARG_IMAGE = "image"
        private const val ARG_TITLE = "title"
        private const val ARG_DESC = "desc"

        fun newInstance(imageRes: Int, title: String, desc: String): OnboardingFragment {
            val fragment = OnboardingFragment()
            val args = Bundle()
            args.putInt(ARG_IMAGE, imageRes)
            args.putString(ARG_TITLE, title)
            args.putString(ARG_DESC, desc)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.onboarding_page, container, false)

        val imageView: ImageView = view.findViewById(R.id.onboarding_image)
        val titleView: TextView = view.findViewById(R.id.onboarding_title)
        val descView: TextView = view.findViewById(R.id.onboarding_desc)

        arguments?.let {
            imageView.setImageResource(it.getInt(ARG_IMAGE))
            titleView.text = it.getString(ARG_TITLE)
            descView.text = it.getString(ARG_DESC)
        }

        return view
    }
}
