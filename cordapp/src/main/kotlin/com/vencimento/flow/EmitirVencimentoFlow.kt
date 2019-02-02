package com.vencimento.flow

import co.paralleluniverse.fibers.Suspendable
import com.vencimento.contract.VencimentoContract
import com.vencimento.state.StatusVencimento
import com.vencimento.state.VencimentoState
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.time.Instant

object EmitirVencimentoFlow {

    @InitiatingFlow
    @StartableByRPC
    class Initiator(
            val para: Party,
            val valor: Int,
            val dataVencimento: Instant): FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {
            //Selecionar o notary
            val notary = serviceHub.networkMapCache.notaryIdentities.first()

            //Construir os states

            val output = VencimentoState(
                    ourIdentity,
                    para,
                    valor,
                    dataVencimento,
                    StatusVencimento.Emitido
            )

            //Construir os comandos

            val comando = Command(VencimentoContract.Commands.Emitir(),
                    listOf(ourIdentity, para).map { it.owningKey })

            //Construir a transação

            val txBuilder = TransactionBuilder(notary)
                    .addCommand(comando)
                    .addOutputState(output, VencimentoContract::class.java.canonicalName)

            // Verificar a transação

            txBuilder.verify(serviceHub)

            //Coletar assinaturas

            val txAssinadaPorMim = serviceHub.signInitialTransaction(txBuilder)

            val sessao = initiateFlow(para)

            val txTotalmenteAssinada = subFlow(
                    CollectSignaturesFlow(txAssinadaPorMim, listOf(sessao)))

            //Notorizar e gravar na base

            return subFlow(FinalityFlow(txTotalmenteAssinada))
        }

    }

    @InitiatedBy(Initiator::class)
    class Acceptor(val otherParty: FlowSession): FlowLogic<SignedTransaction>() {

        @Suspendable
        override fun call(): SignedTransaction {
            val flow = object : SignTransactionFlow(otherParty) {

                override fun checkTransaction(stx: SignedTransaction) {
                }

            }

            return subFlow(flow)
        }

    }
}