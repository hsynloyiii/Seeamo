package com.example.seeamo.utilize.helper

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target

class ImageHelper {
    companion object {
        fun uriToBitmap(
            context: Context,
            uri: Uri?,
            failed: (() -> Unit)? = null,
            success: ((bitmap: Bitmap?) -> Unit)? = null
        ): Bitmap = Glide.with(context)
            .asBitmap()
            .load(uri)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    failed?.invoke()
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    success?.invoke(resource)
                    return false
                }

            })
            .submit()
            .get()

        fun loadUriTo(
            view: ImageView,
            uri: Uri?,
            cornerSize: Int = 1,
            options: BitmapTransitionOptions = BitmapTransitionOptions.withCrossFade(),
            success: ((bitmap: Bitmap?) -> Unit)? = null,
            failed: (() -> Unit)? = null
        ) {
            Glide.with(view)
                .asBitmap()
                .load(uri)
                .transition(options)
                .apply(RequestOptions().transform(RoundedCorners(cornerSize)))
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        failed?.invoke()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        success?.invoke(resource)
                        return false
                    }

                })
                .into(view)
        }

        fun roundBitmapTo(
            view: ImageView,
            source: Bitmap?,
            cornerSize: Int = 0,
            options: BitmapTransitionOptions = BitmapTransitionOptions.withCrossFade(),
            transformation: BitmapTransformation = CenterCrop(),
            failed: (() -> Unit)? = null,
            success: ((bitmap: Bitmap?) -> Unit)? = null
        ) = Glide.with(view)
            .asBitmap()
            .load(source)
            .transition(options)
            .apply(RequestOptions().transform(transformation, RoundedCorners(cornerSize)))
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    failed?.invoke()
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    success?.invoke(resource)
                    return false
                }

            })
            .into(view)
    }
}

