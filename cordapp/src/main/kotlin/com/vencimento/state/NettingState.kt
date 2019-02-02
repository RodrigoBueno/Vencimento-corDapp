package com.vencimento.state

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party

data class NettingState(
        val valorPagamento: Int,
        val de: Party,
        val para: Party,
        val codigoSwift: String,
        val statusNetting: StatusNetting,
        override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState {
    override val participants: List<AbstractParty> = listOf(de, para)
}