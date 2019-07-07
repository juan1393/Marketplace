package com.juangomez.database.sources

import com.juangomez.data.entities.ProductEntity
import com.juangomez.data.sources.database.DatabaseProductsSource
import com.juangomez.database.MarketplaceDatabase
import com.juangomez.database.dao.ProductDao
import com.juangomez.database.entities.DatabaseProductEntity
import com.juangomez.database.mappers.toDatabaseEntity
import com.juangomez.database.mappers.toEntity
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class DatabaseProductsSourceImpl(private val database: MarketplaceDatabase) : DatabaseProductsSource {

    private val productDao: ProductDao = database.productsDao()

    override fun getProducts(): Single<List<ProductEntity>> {
        return productDao.getAll().toEntity()
    }

    override fun insertProducts(products: List<ProductEntity>): Completable {
        return productDao.insertAll(products.toDatabaseEntity())
    }

    override fun deleteProducts(): Completable {
        return productDao.deleteAll()
    }

}