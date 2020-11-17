package com.hcanyz.environmentvariable.setting.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.hcanyz.environmentvariable.IEvManager
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
                    list.add(EvVariantTitleWrap(evHandler.evItemName()))
                    list.addAll(allVariantKvs.map { entry ->
                        EvVariantInfoWrap(entry.key, entry.value, currentVariant == entry.key)
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

    class EvVariantInfoWrap(val name: String, val value: String?, val selected: Boolean) :
        IHolderCreatorName

    class EvVariantHolder(parent: ViewGroup) :
        ZViewHolder<EvVariantInfoWrap>(parent, R.layout.item_ev_variant_vertical) {

        override fun update(data: EvVariantInfoWrap, payloads: List<Any>) {
            super.update(data, payloads)
            val name = findViewById<TextView>(R.id.tv_ev_variant_name)
            val value = findViewById<TextView>(R.id.tv_ev_variant_value)
            val selected = findViewById<View>(R.id.iv_ev_variant_selected)

            name.text = data.name
            value.text = data.value
            selected.visibility = if (data.selected) View.VISIBLE else View.INVISIBLE
        }
    }

    class EvVariantTitleWrap(val title: String) : IHolderCreatorName

    class EvVariantTitleHolder(parent: ViewGroup) :
        ZViewHolder<EvVariantTitleWrap>(parent, R.layout.item_ev_variant_title) {

        override fun update(data: EvVariantTitleWrap, payloads: List<Any>) {
            super.update(data, payloads)
            val title = findViewById<TextView>(R.id.tv_ev_variant_title)

            title.text = data.title
        }
    }
}