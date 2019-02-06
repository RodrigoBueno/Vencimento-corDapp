package com.vencimento.flow

import co.paralleluniverse.fibers.Suspendable
import com.vencimento.contract.VencimentoContract
import com.vencimento.state.StatusVencimento
import com.vencimento.state.VencimentoState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

object AceitarVencimentoFlow {

    @InitiatingFlow
    @StartableByRPC
    class Initiator(val vencimentoId: UUID): FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {
            val input = serviceHub.vaultService
                    .queryBy<VencimentoState>(
                            QueryCriteria.LinearStateQueryCriteria(
                                    uuid = listOf(vencimentoId))).states.single()
            //selecionar notary
            val notary = input.state.notary

            requireThat {
                "Apenas o Para pode aceitar Vencimentos." using (
                        input.state.data.para == ourIdentity )
            }

            //construir outputs
            val output = input.state.data.copy(statusVencimento = StatusVencimento.APagar)
            //construir comando
            val comando = Command(VencimentoContract.Commands.Aceitar(),
                    output.participants.map { it.owningKey })
            //construir transacao
            val txBuilder = TransactionBuilder(notary)
                    .addCommand(comando)
                    .addOutputState(output, VencimentoContract::class.java.canonicalName)
                    .addInputState(input)
            //verificar transacao

            txBuilder.verify(serviceHub)

            //assinar transacao

            val txAssinadaPorMim = serviceHub.signInitialTransaction(txBuilder)
            //coletar assinaturas
            val sessao = initiateFlow(output.de)
            val txTotalmenteAssinada = subFlow(
                    CollectSignaturesFlow(txAssinadaPorMim, listOf(sessao)))
            return subFlow(FinalityFlow(txTotalmenteAssinada))
        }
    }

    @InitiatedBy(Initiator::class)
    class Acceptor(val otherParty: FlowSession): FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {
            val flow = object : SignTransactionFlow(otherParty) {

                override fun checkTransaction(stx: SignedTransaction) {
                    requireThat {
                        "O vencimento não pode estar vencido." using (
                                stx.tx.outputsOfType<VencimentoState>().all {
                                    it.dataVencimento.isAfter(Instant.now().truncatedTo(ChronoUnit.DAYS)) ||
                                            it.dataVencimento.truncatedTo(ChronoUnit.DAYS) == Instant.now().truncatedTo(ChronoUnit.DAYS)
                                } )
                        val stateRef = stx.inputs.single()
                        val input = serviceHub.vaultService.queryBy<VencimentoState>(
                                QueryCriteria.VaultQueryCriteria(
                                        stateRefs = listOf(stateRef)))
                                .states.single().state.data
                        "Apenas o Para pode aceitar Vencimentos." using (
                                input.para == otherSideSession.counterparty)
                    }

                }

            }

            return subFlow(flow)
        }

    }

}