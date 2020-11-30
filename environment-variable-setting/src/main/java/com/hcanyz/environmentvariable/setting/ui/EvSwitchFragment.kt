package com.hcanyz.environmentvariable.setting.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hcanyz.environmentvariable.EvHandler
import com.hcanyz.environmentvariable.IEvManager
import com.hcanyz.environmentvariable.base.EV_VARIANT_PRESET_CUSTOMIZE
import com.hcanyz.environmentvariable.setting.R
import com.hcanyz.environmentvariable.setting.databinding.FragmentEvSwitchBinding
import com.hcanyz.zadapter.ZAdapter
import com.hcanyz.zadapter.helper.bindZAdapter
import com.hcanyz.zadapter.hodler.ViewHolderHelper
import com.hcanyz.zadapter.hodler.ZViewHolder
import com.hcanyz.zadapter.registry.IHolderCreatorName

class EvSwitchFragment : Fragment() {

    companion object {
        fun newInstance(evGroupClass: Class<IEvManager>): Fragment {
            return EvSwitchFragment().apply {
                val args = Bundle()
                args.putSerializable("evGroupClass", evGroupClass)
                arguments = args
            }
        }
    }

    private var evMainBinding: FragmentEvSwitchBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        evMainBinding = FragmentEvSwitchBinding.inflate(inflater, container, false)
        return evMainBinding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        evMainBinding = null
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.run {
            val managerClass: Class<IEvManager> =
                getSerializable("evGroupClass") as Class<IEvManager>

            val iEvManager: IEvManager =
                managerClass.getMethod("getSingleton", Context::class.java)
                    .invoke(null, requireContext()) as IEvManager

            val evHandlers = iEvManager.getEvHandlers(requireContext())

            val list: MutableList<IHolderCreatorName> = evHandlers
                .flatMap { evHandler ->
                    val allVariantKvs = evHandler.allVariantKvs()
                    val currentVariant = evHandler.currentVariant()
                    val list = arrayListOf<IHolderCreatorName>()
                    val itemName = evHandler.evItemName()
                    list.add(EvVariantTitleWrap(itemName))
                    list.addAll(allVariantKvs.map { entry ->
                        EvVariantInfoWrap(
                            evHandler,
                            itemName,
                            entry.key,
                            entry.value,
                            currentVariant == entry.key
                        )
                    })
                    list
                }.toMutableList()

            val listZAdapter = ZAdapter(list)
            listZAdapter.registry
                .registered(EvVariantTitleWrap::class.java.name) { parent: ViewGroup ->
                    return@registered EvVariantTitleHolder(parent)
                }
            listZAdapter.registry
                .registered(EvVariantInfoWrap::class.java.name) { parent: ViewGroup ->
                    return@registered EvVariantHolder(parent)
                }
            evMainBinding?.evRcyList?.bindZAdapter(listZAdapter)

            // 批量切换环境
            val variantList: MutableList<String> =
                iEvManager.intersectionVariants.toMutableList()
            val variantZAdapter =
                ZAdapter(
                    variantList,
                    viewHolderHelper = EvVariantItemViewHolderHelper(
                        this@EvSwitchFragment
                    ) { variantName ->
                        evHandlers.forEach { evHandler ->
                            evHandler.changeVariant(variantName)
                            listZAdapter.datas.forEach {
                                (it as? EvVariantInfoWrap)?.selected =
                                    (it as? EvVariantInfoWrap)?.variantName == variantName
                            }
                            listZAdapter.notifyDataSetChanged()
                        }
                    }
                )
            variantZAdapter.registry.registered { return@registered "single" }
            variantZAdapter.registry
                .registered("single") { parent: ViewGroup ->
                    return@registered EvVariantItemHolder(parent)
                }
            evMainBinding?.evRcyFullVariant?.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            evMainBinding?.evRcyFullVariant?.adapter = variantZAdapter
        }
    }

    class EvVariantItemHolder(parent: ViewGroup) :
        ZViewHolder<String>(parent, R.layout.item_ev_variant_horizontal) {
        private val name by lazy { fv<TextView>(R.id.tv_ev_variant_name) }

        override fun initListener(rootView: View) {
            super.initListener(rootView)
            name.setOnClickListener {
                (mViewHolderHelper as? EvVariantItemViewHolderHelper)?.itemClick?.invoke(mData)
            }
        }

        override fun update(data: String, payloads: List<Any>) {
            super.update(data, payloads)
            name.text = data
        }
    }

    class EvVariantItemViewHolderHelper(
        fragment: Fragment,
        val itemClick: (variantName: String) -> Unit
    ) : ViewHolderHelper(fragment)

    class EvVariantInfoWrap(
        val evHandler: EvHandler,
        val itemName: String,
        val variantName: String,
        val variantValue: String?,
        var selected: Boolean
    ) : IHolderCreatorName

    class EvVariantHolder(parent: ViewGroup) :
        ZViewHolder<EvVariantInfoWrap>(parent, R.layout.item_ev_variant_vertical) {

        private val name by lazy { fv<TextView>(R.id.tv_ev_variant_name) }
        private val valueTv by lazy { fv<TextView>(R.id.tv_ev_variant_value) }
        private val valueEt by lazy { fv<TextView>(R.id.et_ev_variant_value) }
        private val selected by lazy { fv<View>(R.id.iv_ev_variant_selected) }

        override fun initListener(rootView: View) {
            super.initListener(rootView)
            rootView.setOnClickListener {
                (zAdapter as? ZAdapter<*>)?.run {
                    datas.forEach { data ->
                        if (data is EvVariantInfoWrap && mData.itemName == data.itemName) {
                            data.selected = false
                        }
                    }
                    mData.selected = true
                    if (mData.variantName == EV_VARIANT_PRESET_CUSTOMIZE) {
                        mData.evHandler.changeVariantToCustomize(valueEt.text?.toString() ?: "")
                    } else {
                        mData.evHandler.changeVariant(mData.variantName)
                    }
                    notifyDataSetChanged()
                }
            }
            valueEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if (valueEt.hasFocus() && mData.selected) {
                        mData.evHandler.changeVariantToCustomize(s.toString())
                    }
                }
            })
            valueEt.setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    val manager =
                        mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    manager.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }

        override fun update(data: EvVariantInfoWrap, payloads: List<Any>) {
            super.update(data, payloads)
            name.text = data.variantName
            selected.visibility = if (data.selected) View.VISIBLE else View.INVISIBLE
            if (data.variantName == EV_VARIANT_PRESET_CUSTOMIZE) {
                valueTv.visibility = View.GONE
                valueEt.visibility = View.VISIBLE
                valueEt.text = data.variantValue
            } else {
                valueEt.visibility = View.GONE
                valueTv.visibility = View.VISIBLE
                valueTv.text = data.variantValue
            }
        }
    }

    class EvVariantTitleWrap(val title: String) : IHolderCreatorName

    class EvVariantTitleHolder(parent: ViewGroup) :
        ZViewHolder<EvVariantTitleWrap>(parent, R.layout.item_ev_variant_title) {

        override fun update(data: EvVariantTitleWrap, payloads: List<Any>) {
            super.update(data, payloads)
            val title = fv<TextView>(R.id.tv_ev_variant_title)

            title.text = data.title
        }
    }
}