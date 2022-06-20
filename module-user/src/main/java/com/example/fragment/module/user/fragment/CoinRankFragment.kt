package com.example.fragment.module.user.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fragment.library.base.model.BaseViewModel
import com.example.fragment.library.base.view.pull.OnLoadMoreListener
import com.example.fragment.library.base.view.pull.OnRefreshListener
import com.example.fragment.library.base.view.pull.PullRefreshLayout
import com.example.fragment.library.common.constant.Keys
import com.example.fragment.library.common.constant.Router
import com.example.fragment.library.common.dialog.StandardDialog
import com.example.fragment.library.common.fragment.RouterFragment
import com.example.fragment.module.user.adapter.CoinRankAdapter
import com.example.fragment.module.user.databinding.CoinRankFragmentBinding
import com.example.fragment.module.user.model.CoinRankViewModel

class CoinRankFragment : RouterFragment() {

    private val viewModel: CoinRankViewModel by viewModels()
    private var _binding: CoinRankFragmentBinding? = null
    private val binding get() = _binding!!
    private var _coinRankAdapter: CoinRankAdapter? = null
    private val coinRankAdapter get() = _coinRankAdapter!!
    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            backPressedDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CoinRankFragmentBinding.inflate(inflater, container, false)
        _coinRankAdapter = CoinRankAdapter()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity.onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)
    }

    private fun backPressedDialog() {
        StandardDialog.newInstance()
            .setContent("直接回到首页吗？")
            .setOnDialogClickListener(object : StandardDialog.OnDialogClickListener {
                override fun onConfirm(dialog: StandardDialog) {
                    backPressedCallback.isEnabled = true
                    activity.navigation(Router.MAIN)
                }

                override fun onCancel(dialog: StandardDialog) {
                    backPressedCallback.isEnabled = false
                    activity.onBackPressed()
                }
            })
            .show(childFragmentManager)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.listData = coinRankAdapter.getData()
        _coinRankAdapter = null
        _binding = null
    }

    override fun initView() {
        binding.black.setOnClickListener {
            activity.onBackPressed()
        }
        binding.rule.setOnClickListener {
            val url = Uri.encode("https://www.wanandroid.com/blog/show/2653")
            activity.navigation(Router.WEB, bundleOf(Keys.URL to url))
        }
        //积分排行榜列表
        binding.list.layoutManager = LinearLayoutManager(binding.list.context)
        binding.list.adapter = coinRankAdapter
        binding.list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                viewModel.listScroll += dy
            }
        })
        //下拉刷新
        binding.pullRefresh.setOnRefreshListener(object : OnRefreshListener {
            override fun onRefresh(refreshLayout: PullRefreshLayout) {
                viewModel.getCoinRankHome()
            }
        })
        //加载更多
        binding.pullRefresh.setOnLoadMoreListener(binding.list, object : OnLoadMoreListener {
            override fun onLoadMore(refreshLayout: PullRefreshLayout) {
                viewModel.getCoinRankNext()
            }
        })
        //将数据从 ViewModel 取出渲染
        if (viewModel.listData.isNotEmpty()) {
            val names = arrayListOf(binding.name1, binding.name2, binding.name3)
            val coins = arrayListOf(binding.coin1, binding.coin2, binding.coin3)
            val size = if (viewModel.listData.size < 3) viewModel.listData.size else 3
            for (i in 0 until size) {
                names[i].text = viewModel.listData[i].username
                coins[i].text = viewModel.listData[i].coinCount
            }
            if (viewModel.listData.size > 3) {
                coinRankAdapter.setNewData(viewModel.listData.subList(2, viewModel.listData.size))
            }
        }
        if (viewModel.listScroll > 0) {
            binding.list.scrollTo(0, viewModel.listScroll)
        }
        binding.coordinator.post {
            binding.coordinator.setMaxScrollY(binding.coinRankTop.height)
            binding.pullRefresh.layoutParams.height = binding.coordinator.height
        }
    }

    override fun initViewModel(): BaseViewModel {
        viewModel.coinRankResult().observe(viewLifecycleOwner) { result ->
            httpParseSuccess(result) {
                val names = arrayListOf(binding.name1, binding.name2, binding.name3)
                val coins = arrayListOf(binding.coin1, binding.coin2, binding.coin3)
                val datas = it.data?.datas
                if (viewModel.isHomePage() && !datas.isNullOrEmpty()) {
                    val size = if (datas.size < 3) datas.size else 3
                    for (i in 0 until size) {
                        names[i].text = datas[i].username
                        coins[i].text = datas[i].coinCount
                    }
                    if (datas.size > 3) {
                        coinRankAdapter.setNewData(datas.subList(2, datas.size))
                    }
                } else {
                    coinRankAdapter.addData(datas)
                }
            }
            //结束下拉刷新状态
            binding.pullRefresh.finishRefresh()
            //设置加载更多状态
            binding.pullRefresh.setLoadMore(viewModel.hasNextPage())
        }
        return viewModel
    }

}