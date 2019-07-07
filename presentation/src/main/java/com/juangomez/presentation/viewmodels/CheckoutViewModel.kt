package com.juangomez.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.juangomez.data.mappers.toEntity
import com.juangomez.data.mappers.toModel
import com.juangomez.domain.interactors.AddProductUseCase
import com.juangomez.domain.interactors.CreateCheckoutUseCase
import com.juangomez.domain.interactors.DeleteProductUseCase
import com.juangomez.domain.models.cart.Cart
import com.juangomez.domain.models.checkout.Checkout
import com.juangomez.domain.models.product.Product
import com.juangomez.presentation.mappers.toPresentationModel
import com.juangomez.presentation.models.CheckoutPresentationModel
import com.juangomez.presentation.models.ProductPresentationModel
import com.juangomez.presentation.viewmodels.base.BaseViewModel
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.subscribers.DisposableSubscriber

class CheckoutViewModel(
    private val addProductUseCase: AddProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val createCheckoutUseCase: CreateCheckoutUseCase
) : BaseViewModel(), CheckoutListener {

    val isShowingEmptyCase = MutableLiveData<Boolean>()
    val checkoutProductsToShow = MediatorLiveData<List<CheckoutPresentationModel>>()

    private lateinit var cart: Cart

    fun prepare() {
        createCheckout()
    }

    private fun createCheckout() {
        val createCheckoutDisposable = CreateCheckoutSubscriber()
        createCheckoutUseCase.execute(createCheckoutDisposable)
        addDisposable(createCheckoutDisposable)
    }

    override fun onAddProductClicked(code: String) {
        val addProductDisposable = AddProductSubscriber()
        addProductUseCase.execute(addProductDisposable, cart.items.find { it.product.code == code }!!.product)
        addDisposable(addProductDisposable)
    }

    override fun onDeleteProductClicked(code: String) {
        val deleteProductDisposable = DeleteProductSubscriber()
        deleteProductUseCase.execute(deleteProductDisposable, cart.items.find { it.product.code == code }!!.product)
        addDisposable(deleteProductDisposable)
    }

    inner class CreateCheckoutSubscriber : DisposableSubscriber<Checkout>() {

        override fun onComplete() {

        }

        override fun onNext(t: Checkout?) {
            cart = t!!.checkoutCart
            checkoutProductsToShow.postValue(t.toPresentationModel())
            isShowingEmptyCase.postValue(t.checkoutCart.items.isEmpty())
            Log.d("OLE!", "OLE!")
        }

        override fun onError(e: Throwable) {
            Log.d("ERROR!", "ERROR!")
        }

    }

    inner class AddProductSubscriber : DisposableCompletableObserver() {

        override fun onComplete() {
            Log.d("COMPLETADO!", "COMPLETADO!")
        }

        override fun onError(e: Throwable) {
            Log.d("ERROR!", "ERROR!")
        }

    }

    inner class DeleteProductSubscriber : DisposableCompletableObserver() {

        override fun onComplete() {
            Log.d("COMPLETADO!", "COMPLETADO!")
        }

        override fun onError(e: Throwable) {
            Log.d("ERROR!", "ERROR!")
        }

    }

}

interface CheckoutListener {
    fun onAddProductClicked(code: String)

    fun onDeleteProductClicked(code: String)
}