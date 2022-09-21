package com.sandip.notefy.ui.user

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import com.sandip.notefy.R
import com.sandip.notefy.databinding.FragmentUserBinding

class User : Fragment(R.layout.fragment_user) {

    companion object {
        fun newInstance() = User()
    }

    private lateinit var viewModel: UserViewModel
    private lateinit var binding: FragmentUserBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserBinding.bind(view)

        binding.apply {
            startAnimation(notesNumber)
            startAnimation(reminderNumber)
            startAnimation(todoNumber)
            editName.setOnClickListener {
                textName.requestFocus(View.LAYOUT_DIRECTION_LTR)

            }
            editEmail.setOnClickListener {
                textEmail.requestFocus(View.LAYOUT_DIRECTION_LTR)

            }



        }
    }

    private fun startAnimation(notesNumber: TextView) {
        val animator = ValueAnimator.ofInt(0, 100)
        animator.duration = 5000 // 5 seconds
        animator.addUpdateListener { animation ->
            notesNumber.text = animation.animatedValue.toString()
        }
        animator.start()
    }
}