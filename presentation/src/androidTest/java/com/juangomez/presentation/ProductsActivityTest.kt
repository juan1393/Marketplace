package com.juangomez.presentation

import android.view.View
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.juangomez.domain.models.product.Product
import com.juangomez.domain.repositories.ProductRepository
import com.juangomez.presentation.idling.DataBindingIdlingResourceRule
import com.juangomez.presentation.idling.ViewVisibilityIdlingResource
import com.juangomez.presentation.mappers.toPresentationModel
import com.juangomez.presentation.models.ProductPresentationModel
import com.juangomez.presentation.recyclerview.RecyclerViewInteraction
import com.juangomez.presentation.recyclerview.matcher.RecyclerViewItemsCountMatcher.Companion.recyclerViewHasItemCount
import com.juangomez.presentation.util.minDelay
import com.juangomez.presentation.views.ProductsActivity
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.reactivex.Single
import org.hamcrest.Matchers.not
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.module.module
import org.koin.standalone.StandAloneContext.loadKoinModules
import org.koin.standalone.StandAloneContext.stopKoin


@RunWith(AndroidJUnit4::class)
class ProductsActivityTest {

    companion object {

        private const val DEFAULT_LIST_SIZE = 10
        private const val DEFAULT_MILLISECONDS_TO_LOAD_PRODUCTS_DELAY = 2000L

        @MockK
        lateinit var productRepository: ProductRepository

        @BeforeClass
        @JvmStatic
        fun setup() {
            MockKAnnotations.init(this)

            loadKoinModules(module {
                single(override = true) {
                    productRepository
                }
            })
        }

        @AfterClass
        fun tearDown() {
            stopKoin()
        }
    }

    @get:Rule
    var activityTestRule = ActivityTestRule(
        ProductsActivity::class.java, true,
        false
    )

    @get:Rule
    val dataBindingIdlingResourceRule = DataBindingIdlingResourceRule(activityTestRule)

    @Test
    @Throws(InterruptedException::class)
    fun shouldShowEmptyCaseIfThereAreNoProducts() {
        givenThereAreNoProducts()

        startActivity()

        onView(withId(R.id.empty_case)).check(matches(isDisplayed()))
    }

    @Test
    @Throws(InterruptedException::class)
    fun shouldShowProductsIfThereAreProducts() {
        val products = givenThereAreDifferentProducts(DEFAULT_LIST_SIZE)
        val productsPresentation = products.toPresentationModel()

        startActivity()

        RecyclerViewInteraction.onRecyclerView<ProductPresentationModel>(withId(R.id.recycler_view))
            .withItems(productsPresentation)
            .check(object : RecyclerViewInteraction.ItemViewAssertion<ProductPresentationModel> {
                override fun check(
                    item: ProductPresentationModel,
                    view: View,
                    e: NoMatchingViewException?
                ) {
                    matches(hasDescendant(withText(item.name))).check(view, e)
                    matches(hasDescendant(withText(item.price))).check(view, e)
                }
            })
    }

    @Test
    @Throws(InterruptedException::class)
    fun shouldHideEmptyCaseIfThereAreProducts() {
        givenThereAreDifferentProducts(DEFAULT_LIST_SIZE)

        startActivity()

        onView(withId(R.id.empty_case)).check(matches(not(isDisplayed())))
    }

    @Test
    @Throws(InterruptedException::class)
    fun shouldHideLoadingIfThereAreProducts() {
        givenThereAreDifferentProducts(DEFAULT_LIST_SIZE)

        startActivity()

        onView(withId(R.id.progress_bar)).check(matches(not(isDisplayed())))
    }

    @Test
    @Throws(InterruptedException::class)
    fun shouldShowLoadingIfProductsAreLoading() {
        givenThereAreNoProducts(DEFAULT_MILLISECONDS_TO_LOAD_PRODUCTS_DELAY)

        startActivity()

        onView(withId(R.id.progress_bar)).check(matches(isDisplayed()))
    }

    @Test
    @Throws(InterruptedException::class)
    fun shouldShowTheExactNumberOfProducts() {
        givenThereAreDifferentProducts(DEFAULT_LIST_SIZE)

        startActivity()

        onView(withId(R.id.recycler_view)).check(
            matches(recyclerViewHasItemCount(DEFAULT_LIST_SIZE))
        )
    }

    @Test
    @Throws(InterruptedException::class)
    fun shouldAddProductToCartOnRecyclerViewItemTapped() {
        givenThereAreDifferentProducts(DEFAULT_LIST_SIZE)
        val productIndex = 0

        startActivity()

        onView(withId(R.id.recycler_view)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                productIndex,
                click()
            )
        )

        val cartGroup: Group = activityTestRule.activity.findViewById(R.id.cart_group)
        val idlingResource = ViewVisibilityIdlingResource(cartGroup, View.VISIBLE)

        IdlingRegistry.getInstance().register(idlingResource)
        onView(withId(R.id.cart_group)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        IdlingRegistry.getInstance().unregister(idlingResource)
    }

    private fun startActivity() {
        activityTestRule.launchActivity(null)
    }

    private fun givenThereAreNoProducts(delay: Long = 0) {
        every { productRepository.getProducts() } returns Single.just(emptyList<Product>()).minDelay(delay)
    }

    private fun givenThereAreDifferentProducts(amount: Int): List<Product> {
        val defaultCode = "CODE"
        val defaultProduct = "PRODUCT"
        val defaultPrice = 100f

        val products = (1..amount).map {
            Product(
                "$defaultCode$it",
                "$defaultProduct$it",
                defaultPrice
            )
        }

        every { productRepository.getProducts() } returns Single.just(products)

        return products
    }
}