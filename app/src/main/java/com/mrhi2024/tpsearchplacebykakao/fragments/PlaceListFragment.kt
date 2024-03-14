package com.mrhi2024.tpsearchplacebykakao.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mrhi2024.tpsearchplacebykakao.activities.MainActivity
import com.mrhi2024.tpsearchplacebykakao.adapter.PlaceListRecyclerAdapter
import com.mrhi2024.tpsearchplacebykakao.data.Place
import com.mrhi2024.tpsearchplacebykakao.databinding.FragmentPlaceListBinding

class PlaceListFragment : Fragment() {

    private val binding:FragmentPlaceListBinding by lazy { FragmentPlaceListBinding.inflate(layoutInflater) }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //리사이클러뷰에 MainActivity가 가지고 있는 대량의 장소정보를 보여지도록
        val ma:MainActivity = activity as MainActivity
        ma.searchPlaceResponse ?: return // 아직 서버로딩이 완료되지 않았을 수도 있어서
        binding.recyclerView.adapter = PlaceListRecyclerAdapter(requireContext(), ma.searchPlaceResponse!!.documents)


    }

}