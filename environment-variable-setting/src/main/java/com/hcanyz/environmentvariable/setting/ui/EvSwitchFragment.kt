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
import com.hcanyz.environmentvariable.EvHandler
import com.hcanyz.environmentvariable.IEvManager
import com.hcanyz.environmentvariable.base.EV_VARIANT_PRESET_CUSTOMIZE
import com.hcanyz.environmentvariable.setting.R
import com.hcanyz.zadapter.ZAdapter
import com.hcanyz.zadapter.helper.bindZAdapter
import com.hcanyz.zadapter.hodler.ZViewHolder
import com.hcanyz.zadapter.registry.IHolderCreatorName
import kotlinx.android.synthetic.main.fragment_ev_switch.*

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ev_switch, container, false)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.run {
            val managerClass: Class<IEvManager> =
                getSerializable("evGroupClass") as Class<IEvManager>

            val iEvManager: IEvManager =
                managerClass.getMethod("getSingleton").invoke(null) as IEvManager

            val list: MutableList<IHolderCreatorName> = iEvManager.getEvHandlers(requireContext())
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

            val zAdapter = ZAdapter(list)
            zAdapter.registry
                .registered(EvVariantTitleWrap::class.java.name) { parent: ViewGroup ->
                    return@registered EvVariantTitleHolder(parent)
                }
            zAdapter.registry
                .registered(EvVariantInfoWrap::class.java.name) { parent: ViewGroup ->
                    return@registered EvVariantHolder(parent)
                }
            ev_rcy_list.bindZAdapter(zAdapter)
        }
    }

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
                    mData.evHandler.changeVariant(mData.variantName)
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
                    if (valueEt.hasFocus()) {
                        mData.evHandler.changeVariantToCustomize(s.toString())
                    }
                }
            })
            valueEt.setOnFocusChangeListener { v, hasFocus ->
                val manager =
                    mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if (!hasFocus) {
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