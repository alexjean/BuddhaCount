package com.tomorrow_eyes.buddhacount

import android.annotation.SuppressLint
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.tomorrow_eyes.buddhacount.databinding.FragmentKsitigarbhaBinding
import java.io.IOException
import java.time.LocalDate

class KsitigarbhaFragment : Fragment() {
    private var binding: FragmentKsitigarbhaBinding? = null
    private var viewModel: MyViewModel? = null
    // private var bgColor: Color? = null
    private var mPlayer: MediaPlayer? = null
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentKsitigarbhaBinding.inflate(inflater, container, false)
        val activity = requireActivity() as MainActivity
        viewModel = ViewModelProvider(activity)[MyViewModel::class.java]
        val actionBar = activity.supportActionBar
        actionBar?.title = viewModel!!.title
        return binding!!.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val _viewModel = viewModel ?: return;
        val _binding = binding ?: return;
        _viewModel.ReadCountFromFile(context);
        _binding.textviewFirst.text = _viewModel.countString
        // bgColor = themeColorOnPrimary
        mPlayer = MediaPlayer.create(context, R.raw.wooden_knocker)
        try {
            mPlayer?.prepare()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        _binding.buttonCount.setOnTouchListener { _: View?, event: MotionEvent ->
            if (event.action != MotionEvent.ACTION_DOWN) return@setOnTouchListener true
            if (_viewModel.woodenKnocker) {
                if (mPlayer?.isPlaying == true) {
                    mPlayer?.pause()
                    mPlayer?.seekTo(0)
                    // mPlayer.stop();
                }
                mPlayer?.start()
            }
            _viewModel.addCount()
            _binding.textviewFirst.text = _viewModel.countString
            _viewModel.mark = LocalDate.now()
            _viewModel.writeCountToFile(context)
            true // true 不處理OnClick
        }
        _binding.buttonCount.setOnClickListener { v: View? -> }
        if (_viewModel.darkBackground) {
            view.setBackgroundColor(0xFF000000.toInt())   // Kotlin treat 0xFF00000 as long or uint
            val color = resources.getColor(R.color.alex_background, requireContext().theme)
            _binding.textviewFirst.setTextColor(color)
        }
    }

    override fun onDestroyView() {
        mPlayer?.release()
        super.onDestroyView()
        binding = null
    }

    private val themeColorOnPrimary: Color
        get() {
            val theme = requireActivity().theme
            val typedValue = TypedValue()
            theme.resolveAttribute(R.attr.colorOnPrimary, typedValue, true)
            return Color.valueOf(typedValue.data)
        }
}