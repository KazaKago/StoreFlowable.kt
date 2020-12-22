package com.kazakago.storeflowable.sample.viewmodel.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <A, B> compositeLiveDataOf(first: LiveData<A>, second: LiveData<B>): LiveData<Pair<A, B>> = PairLiveData(Pair(first, second))
fun <A, B, C> compositeLiveDataOf(first: LiveData<A>, second: LiveData<B>, third: LiveData<C>): LiveData<Triple<A, B, C>> = TripleLiveData(Triple(first, second, third))
fun <A, B, C, D> compositeLiveDataOf(first: LiveData<A>, second: LiveData<B>, third: LiveData<C>, fourth: LiveData<D>): LiveData<Quartet<A, B, C, D>> = QuartetLiveData(Quartet(first, second, third, fourth))
fun <A, B, C, D, E> compositeLiveDataOf(first: LiveData<A>, second: LiveData<B>, third: LiveData<C>, fourth: LiveData<D>, fifth: LiveData<E>): LiveData<Quintet<A, B, C, D, E>> = QuintetLiveData(Quintet(first, second, third, fourth, fifth))
fun <A, B, C, D, E, F> compositeLiveDataOf(first: LiveData<A>, second: LiveData<B>, third: LiveData<C>, fourth: LiveData<D>, fifth: LiveData<E>, sixth: LiveData<F>): LiveData<Sextet<A, B, C, D, E, F>> = SextetLiveData(Sextet(first, second, third, fourth, fifth, sixth))
fun <A, B, C, D, E, F, G> compositeLiveDataOf(first: LiveData<A>, second: LiveData<B>, third: LiveData<C>, fourth: LiveData<D>, fifth: LiveData<E>, sixth: LiveData<F>, seventh: LiveData<G>): LiveData<Septet<A, B, C, D, E, F, G>> = SeptetLiveData(Septet(first, second, third, fourth, fifth, sixth, seventh))
fun <A, B, C, D, E, F, G, H> compositeLiveDataOf(first: LiveData<A>, second: LiveData<B>, third: LiveData<C>, fourth: LiveData<D>, fifth: LiveData<E>, sixth: LiveData<F>, seventh: LiveData<G>, eighth: LiveData<H>): LiveData<Octet<A, B, C, D, E, F, G, H>> = OctetLiveData(Octet(first, second, third, fourth, fifth, sixth, seventh, eighth))
fun <A, B, C, D, E, F, G, H, I> compositeLiveDataOf(first: LiveData<A>, second: LiveData<B>, third: LiveData<C>, fourth: LiveData<D>, fifth: LiveData<E>, sixth: LiveData<F>, seventh: LiveData<G>, eighth: LiveData<H>, ninth: LiveData<I>): LiveData<Ennead<A, B, C, D, E, F, G, H, I>> = EnneadLiveData(Ennead(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth))

data class Quartet<out A, out B, out C, out D>(val first: A, val second: B, val third: C, val fourth: D)
data class Quintet<out A, out B, out C, out D, out E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)
data class Sextet<out A, out B, out C, out D, out E, out F>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E, val sixth: F)
data class Septet<out A, out B, out C, out D, out E, out F, out G>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E, val sixth: F, val seventh: G)
data class Octet<out A, out B, out C, out D, out E, out F, out G, out H>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E, val sixth: F, val seventh: G, val eighth: H)
data class Ennead<out A, out B, out C, out D, out E, out F, out G, out H, out I>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E, val sixth: F, val seventh: G, val eighth: H, val ninth: I)

internal class PairLiveData<A, B>(private val compositeLiveData: Pair<LiveData<A>, LiveData<B>>) : LiveData<Pair<A, B>>() {

    override fun observe(owner: LifecycleOwner, observer: Observer<in Pair<A, B>>) {
        super.observe(owner, observer)
        compositeLiveData.first.observe(owner) { setCurrentValue() }
        compositeLiveData.second.observe(owner) { setCurrentValue() }
    }

    override fun observeForever(observer: Observer<in Pair<A, B>>) {
        super.observeForever(observer)
        compositeLiveData.first.observeForever { setCurrentValue() }
        compositeLiveData.second.observeForever { setCurrentValue() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun setCurrentValue() {
        val first = compositeLiveData.first.value as A
        val second = compositeLiveData.second.value as B
        value = Pair(first, second)
    }

}

internal class TripleLiveData<A, B, C>(private val compositeLiveData: Triple<LiveData<A>, LiveData<B>, LiveData<C>>) : LiveData<Triple<A, B, C>>() {

    override fun observe(owner: LifecycleOwner, observer: Observer<in Triple<A, B, C>>) {
        super.observe(owner, observer)
        compositeLiveData.first.observe(owner) { setCurrentValue() }
        compositeLiveData.second.observe(owner) { setCurrentValue() }
        compositeLiveData.third.observe(owner) { setCurrentValue() }
    }

    override fun observeForever(observer: Observer<in Triple<A, B, C>>) {
        super.observeForever(observer)
        compositeLiveData.first.observeForever { setCurrentValue() }
        compositeLiveData.second.observeForever { setCurrentValue() }
        compositeLiveData.third.observeForever { setCurrentValue() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun setCurrentValue() {
        val first = compositeLiveData.first.value as A
        val second = compositeLiveData.second.value as B
        val third = compositeLiveData.third.value as C
        value = Triple(first, second, third)
    }

}

internal class QuartetLiveData<A, B, C, D>(private val compositeLiveData: Quartet<LiveData<A>, LiveData<B>, LiveData<C>, LiveData<D>>) : LiveData<Quartet<A, B, C, D>>() {

    override fun observe(owner: LifecycleOwner, observer: Observer<in Quartet<A, B, C, D>>) {
        super.observe(owner, observer)
        compositeLiveData.first.observe(owner) { setCurrentValue() }
        compositeLiveData.second.observe(owner) { setCurrentValue() }
        compositeLiveData.third.observe(owner) { setCurrentValue() }
        compositeLiveData.fourth.observe(owner) { setCurrentValue() }
    }

    override fun observeForever(observer: Observer<in Quartet<A, B, C, D>>) {
        super.observeForever(observer)
        compositeLiveData.first.observeForever { setCurrentValue() }
        compositeLiveData.second.observeForever { setCurrentValue() }
        compositeLiveData.third.observeForever { setCurrentValue() }
        compositeLiveData.fourth.observeForever { setCurrentValue() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun setCurrentValue() {
        val first = compositeLiveData.first.value as A
        val second = compositeLiveData.second.value as B
        val third = compositeLiveData.third.value as C
        val fourth = compositeLiveData.fourth.value as D
        value = Quartet(first, second, third, fourth)
    }

}

internal class QuintetLiveData<A, B, C, D, E>(private val compositeLiveData: Quintet<LiveData<A>, LiveData<B>, LiveData<C>, LiveData<D>, LiveData<E>>) : LiveData<Quintet<A, B, C, D, E>>() {

    override fun observe(owner: LifecycleOwner, observer: Observer<in Quintet<A, B, C, D, E>>) {
        super.observe(owner, observer)
        compositeLiveData.first.observe(owner) { setCurrentValue() }
        compositeLiveData.second.observe(owner) { setCurrentValue() }
        compositeLiveData.third.observe(owner) { setCurrentValue() }
        compositeLiveData.fourth.observe(owner) { setCurrentValue() }
        compositeLiveData.fifth.observe(owner) { setCurrentValue() }
    }

    override fun observeForever(observer: Observer<in Quintet<A, B, C, D, E>>) {
        super.observeForever(observer)
        compositeLiveData.first.observeForever { setCurrentValue() }
        compositeLiveData.second.observeForever { setCurrentValue() }
        compositeLiveData.third.observeForever { setCurrentValue() }
        compositeLiveData.fourth.observeForever { setCurrentValue() }
        compositeLiveData.fifth.observeForever { setCurrentValue() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun setCurrentValue() {
        val first = compositeLiveData.first.value as A
        val second = compositeLiveData.second.value as B
        val third = compositeLiveData.third.value as C
        val fourth = compositeLiveData.fourth.value as D
        val fifth = compositeLiveData.fifth.value as E
        value = Quintet(first, second, third, fourth, fifth)
    }

}

internal class SextetLiveData<A, B, C, D, E, F>(private val compositeLiveData: Sextet<LiveData<A>, LiveData<B>, LiveData<C>, LiveData<D>, LiveData<E>, LiveData<F>>) : LiveData<Sextet<A, B, C, D, E, F>>() {

    override fun observe(owner: LifecycleOwner, observer: Observer<in Sextet<A, B, C, D, E, F>>) {
        super.observe(owner, observer)
        compositeLiveData.first.observe(owner) { setCurrentValue() }
        compositeLiveData.second.observe(owner) { setCurrentValue() }
        compositeLiveData.third.observe(owner) { setCurrentValue() }
        compositeLiveData.fourth.observe(owner) { setCurrentValue() }
        compositeLiveData.fifth.observe(owner) { setCurrentValue() }
        compositeLiveData.sixth.observe(owner) { setCurrentValue() }
    }

    override fun observeForever(observer: Observer<in Sextet<A, B, C, D, E, F>>) {
        super.observeForever(observer)
        compositeLiveData.first.observeForever { setCurrentValue() }
        compositeLiveData.second.observeForever { setCurrentValue() }
        compositeLiveData.third.observeForever { setCurrentValue() }
        compositeLiveData.fourth.observeForever { setCurrentValue() }
        compositeLiveData.fifth.observeForever { setCurrentValue() }
        compositeLiveData.sixth.observeForever { setCurrentValue() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun setCurrentValue() {
        val first = compositeLiveData.first.value as A
        val second = compositeLiveData.second.value as B
        val third = compositeLiveData.third.value as C
        val fourth = compositeLiveData.fourth.value as D
        val fifth = compositeLiveData.fifth.value as E
        val sixth = compositeLiveData.sixth.value as F
        value = Sextet(first, second, third, fourth, fifth, sixth)
    }

}

internal class SeptetLiveData<A, B, C, D, E, F, G>(private val compositeLiveData: Septet<LiveData<A>, LiveData<B>, LiveData<C>, LiveData<D>, LiveData<E>, LiveData<F>, LiveData<G>>) : LiveData<Septet<A, B, C, D, E, F, G>>() {

    override fun observe(owner: LifecycleOwner, observer: Observer<in Septet<A, B, C, D, E, F, G>>) {
        super.observe(owner, observer)
        compositeLiveData.first.observe(owner) { setCurrentValue() }
        compositeLiveData.second.observe(owner) { setCurrentValue() }
        compositeLiveData.third.observe(owner) { setCurrentValue() }
        compositeLiveData.fourth.observe(owner) { setCurrentValue() }
        compositeLiveData.fifth.observe(owner) { setCurrentValue() }
        compositeLiveData.sixth.observe(owner) { setCurrentValue() }
        compositeLiveData.seventh.observe(owner) { setCurrentValue() }
    }

    override fun observeForever(observer: Observer<in Septet<A, B, C, D, E, F, G>>) {
        super.observeForever(observer)
        compositeLiveData.first.observeForever { setCurrentValue() }
        compositeLiveData.second.observeForever { setCurrentValue() }
        compositeLiveData.third.observeForever { setCurrentValue() }
        compositeLiveData.fourth.observeForever { setCurrentValue() }
        compositeLiveData.fifth.observeForever { setCurrentValue() }
        compositeLiveData.sixth.observeForever { setCurrentValue() }
        compositeLiveData.seventh.observeForever { setCurrentValue() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun setCurrentValue() {
        val first = compositeLiveData.first.value as A
        val second = compositeLiveData.second.value as B
        val third = compositeLiveData.third.value as C
        val fourth = compositeLiveData.fourth.value as D
        val fifth = compositeLiveData.fifth.value as E
        val sixth = compositeLiveData.sixth.value as F
        val seventh = compositeLiveData.seventh.value as G
        value = Septet(first, second, third, fourth, fifth, sixth, seventh)
    }

}

internal class OctetLiveData<A, B, C, D, E, F, G, H>(private val compositeLiveData: Octet<LiveData<A>, LiveData<B>, LiveData<C>, LiveData<D>, LiveData<E>, LiveData<F>, LiveData<G>, LiveData<H>>) : LiveData<Octet<A, B, C, D, E, F, G, H>>() {

    override fun observe(owner: LifecycleOwner, observer: Observer<in Octet<A, B, C, D, E, F, G, H>>) {
        super.observe(owner, observer)
        compositeLiveData.first.observe(owner) { setCurrentValue() }
        compositeLiveData.second.observe(owner) { setCurrentValue() }
        compositeLiveData.third.observe(owner) { setCurrentValue() }
        compositeLiveData.fourth.observe(owner) { setCurrentValue() }
        compositeLiveData.fifth.observe(owner) { setCurrentValue() }
        compositeLiveData.sixth.observe(owner) { setCurrentValue() }
        compositeLiveData.seventh.observe(owner) { setCurrentValue() }
        compositeLiveData.eighth.observe(owner) { setCurrentValue() }
    }

    override fun observeForever(observer: Observer<in Octet<A, B, C, D, E, F, G, H>>) {
        super.observeForever(observer)
        compositeLiveData.first.observeForever { setCurrentValue() }
        compositeLiveData.second.observeForever { setCurrentValue() }
        compositeLiveData.third.observeForever { setCurrentValue() }
        compositeLiveData.fourth.observeForever { setCurrentValue() }
        compositeLiveData.fifth.observeForever { setCurrentValue() }
        compositeLiveData.sixth.observeForever { setCurrentValue() }
        compositeLiveData.seventh.observeForever { setCurrentValue() }
        compositeLiveData.eighth.observeForever { setCurrentValue() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun setCurrentValue() {
        val first = compositeLiveData.first.value as A
        val second = compositeLiveData.second.value as B
        val third = compositeLiveData.third.value as C
        val fourth = compositeLiveData.fourth.value as D
        val fifth = compositeLiveData.fifth.value as E
        val sixth = compositeLiveData.sixth.value as F
        val seventh = compositeLiveData.seventh.value as G
        val eighth = compositeLiveData.eighth.value as H
        value = Octet(first, second, third, fourth, fifth, sixth, seventh, eighth)
    }

}

internal class EnneadLiveData<A, B, C, D, E, F, G, H, I>(private val compositeLiveData: Ennead<LiveData<A>, LiveData<B>, LiveData<C>, LiveData<D>, LiveData<E>, LiveData<F>, LiveData<G>, LiveData<H>, LiveData<I>>) : LiveData<Ennead<A, B, C, D, E, F, G, H, I>>() {

    override fun observe(owner: LifecycleOwner, observer: Observer<in Ennead<A, B, C, D, E, F, G, H, I>>) {
        super.observe(owner, observer)
        compositeLiveData.first.observe(owner) { setCurrentValue() }
        compositeLiveData.second.observe(owner) { setCurrentValue() }
        compositeLiveData.third.observe(owner) { setCurrentValue() }
        compositeLiveData.fourth.observe(owner) { setCurrentValue() }
        compositeLiveData.fifth.observe(owner) { setCurrentValue() }
        compositeLiveData.sixth.observe(owner) { setCurrentValue() }
        compositeLiveData.seventh.observe(owner) { setCurrentValue() }
        compositeLiveData.eighth.observe(owner) { setCurrentValue() }
        compositeLiveData.ninth.observe(owner) { setCurrentValue() }
    }

    override fun observeForever(observer: Observer<in Ennead<A, B, C, D, E, F, G, H, I>>) {
        super.observeForever(observer)
        compositeLiveData.first.observeForever { setCurrentValue() }
        compositeLiveData.second.observeForever { setCurrentValue() }
        compositeLiveData.third.observeForever { setCurrentValue() }
        compositeLiveData.fourth.observeForever { setCurrentValue() }
        compositeLiveData.fifth.observeForever { setCurrentValue() }
        compositeLiveData.sixth.observeForever { setCurrentValue() }
        compositeLiveData.seventh.observeForever { setCurrentValue() }
        compositeLiveData.eighth.observeForever { setCurrentValue() }
        compositeLiveData.ninth.observeForever { setCurrentValue() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun setCurrentValue() {
        val first = compositeLiveData.first.value as A
        val second = compositeLiveData.second.value as B
        val third = compositeLiveData.third.value as C
        val fourth = compositeLiveData.fourth.value as D
        val fifth = compositeLiveData.fifth.value as E
        val sixth = compositeLiveData.sixth.value as F
        val seventh = compositeLiveData.seventh.value as G
        val eighth = compositeLiveData.eighth.value as H
        val ninth = compositeLiveData.ninth.value as I
        value = Ennead(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth)
    }
}
