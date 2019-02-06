package com.vencimento.state

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable
import java.time.Instant

@CordaSerializable
data class VencimentoState(
        val de: Party,
        val para: Party,
        val valor: Int,
        val dataVencimento: Instant,
        val statusVencimento: StatusVencimento,
        override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {
    override val participants: List<AbstractParty> = listOf(de, para)
}