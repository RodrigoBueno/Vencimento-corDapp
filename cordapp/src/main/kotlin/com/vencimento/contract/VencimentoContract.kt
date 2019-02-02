package com.vencimento.contract

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireThat
import net.corda.core.transactions.LedgerTransaction

class VencimentoContract : Contract {

    override fun verify(tx: LedgerTransaction) {
        val comando = tx.commandsOfType<Commands>().single()

        when (comando.value) {
            is Commands.Emitir -> verifyEmitir(tx)
            is Commands.Aceitar -> verifyAceitar(tx)
            is Commands.Recusar -> verifyRecusar(tx)
            is Commands.Pagar -> verifyPagar(tx)
        }

        requireThat {
            // regras validas para todos os comandos
            "Todos os participantes devem assinar a transação." using
                    tx.outputs.all {
                        val todosOsAssinantes = tx.commands.flatMap { it.signers }
                        val chavesDosParticipantes = it.data.participants
                                .map { it.owningKey }

                        todosOsAssinantes.containsAll(chavesDosParticipantes)
                    }
        }
    }

    private fun verifyAceitar(tx: LedgerTransaction) {
    }

    private fun verifyEmitir(tx: LedgerTransaction) {
    }

    private fun verifyPagar(tx: LedgerTransaction) {
    }

    private fun verifyRecusar(tx: LedgerTransaction) {
    }

    interface Commands : CommandData {
        class Emitir : Commands
        class Aceitar : Commands
        class Recusar : Commands
        class Pagar : Commands
    }

}