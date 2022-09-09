package com.sandip.notefy.ui.recycle_bin

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sandip.notefy.R

class RecycleBin : Fragment(R.layout.fragment_recycle_bin) {

    companion object {
        fun newInstance() = RecycleBin()
    }

    private lateinit var viewModel: RecycleBinViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recycle_bin, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(RecycleBinViewModel::class.java)
        // TODO: Use the ViewModel
    }

}