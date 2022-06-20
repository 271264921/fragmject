package com.example.fragment.module.wan.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fragment.library.base.model.BaseViewModel
import com.example.fragment.library.common.fragment.RouterFragment
import com.example.fragment.module.wan.adapter.SystemAdapter
import com.example.fragment.module.wan.databinding.NavigationSystemFragmentBinding
import com.example.fragment.module.wan.model.SystemTreeViewModel

class NavigationSystemFragment : RouterFragment() {

    companion object {
        @JvmStatic
        fun newInstance(): NavigationSystemFragment {
            return NavigationSystemFragment()
        }
    }

    private val viewModel: SystemTreeViewModel by activityViewModels()
    private var _binding: NavigationSystemFragmentBinding? = null
    private val binding get() = _binding!!
    private var _systemAdapter: SystemAdapter? = null
    private val systemAdapter get() = _systemAdapter!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NavigationSystemFragmentBinding.inflate(inflater, container, false)
        _systemAdapter = SystemAdapter()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.listData = systemAdapter.getData()
        _systemAdapter = null
        _binding = null
    }

    override fun initView() {
        //体系列表
        binding.list.layoutManager = LinearLayoutManager(binding.list.context)
        binding.list.adapter = systemAdapter
        binding.list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                viewModel.listScroll += dy
            }
        })
    }

    override fun initViewModel(): BaseViewModel {
        viewModel.systemTreeResult().observe(viewLifecycleOwner) { systemAdapter.setNewData(it) }
        return viewModel
    }

}