package com.ex.mdview.presentation.ui.util

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Делегат для работы с ViewBinding в Fragment и Activity.
 * Автоматически очищает binding при уничтожении View.
 */
class ViewBindingDelegate<T : ViewBinding>(
    val fragment: Fragment,
    val bind: (View) -> T
) : ReadOnlyProperty<Fragment, T> {
    private var binding: T? = null
    private var isObserverAdded = false

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        val viewLifecycleOwner = fragment.viewLifecycleOwner
        if (binding == null) {
            binding = bind(thisRef.requireView())
            if (!isObserverAdded) {
                viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
                    override fun onDestroy(owner: LifecycleOwner) {
                        binding = null
                        isObserverAdded = false
                    }
                })
                isObserverAdded = true
            }
        }
        return binding!!
    }
}

inline fun <reified T : ViewBinding> Fragment.viewBinding(
    crossinline bind: (View) -> T
): ViewBindingDelegate<T> = ViewBindingDelegate(this) { view -> bind(view) }

inline fun <reified T : ViewBinding> AppCompatActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T
) = lazy(LazyThreadSafetyMode.NONE) {
    bindingInflater.invoke(layoutInflater)
}

