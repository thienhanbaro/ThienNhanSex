package com.example.spnew2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.spnew2.databinding.FragmentPlaceholderBinding

class PlaceholderFragment : Fragment() {

    companion object {
        private const val ARG_ICON  = "icon"
        private const val ARG_TITLE = "title"
        private const val ARG_SUB   = "sub"

        fun newInstance(icon: String, title: String, sub: String) =
            PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ICON,  icon)
                    putString(ARG_TITLE, title)
                    putString(ARG_SUB,   sub)
                }
            }
    }

    private var _binding: FragmentPlaceholderBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceholderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            binding.tvPlaceholderIcon.text  = it.getString(ARG_ICON,  "⚙")
            binding.tvPlaceholderTitle.text = it.getString(ARG_TITLE, "ĐANG PHÁT TRIỂN")
            binding.tvPlaceholderSub.text   = it.getString(ARG_SUB,   "Sắp ra mắt")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
