package com.template

import com.vencimento.flow.AceitarVencimentoFlow
import com.vencimento.flow.EmitirVencimentoFlow
import com.vencimento.state.StatusVencimento
import com.vencimento.state.VencimentoState
import net.corda.core.contracts.TransactionVerificationException
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.getOrThrow
import net.corda.node.internal.StartedNode
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetwork.MockNode
import net.corda.testing.setCordappPackages
import net.corda.testing.unsetCordappPackages
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FlowTests {
    lateinit var network: MockNetwork
    lateinit var a: StartedNode<MockNode>
    lateinit var b: StartedNode<MockNode>

    @Before
    fun setup() {
        setCordappPackages("com.vencimento")
        network = MockNetwork()
        val nodes = network.createSomeNodes(2)
        a = nodes.partyNodes[0]
        b = nodes.partyNodes[1]
        nodes.partyNodes.forEach {
            it.registerInitiatedFlow(EmitirVencimentoFlow.Acceptor::class.java)
            it.registerInitiatedFlow(AceitarVencimentoFlow.Acceptor::class.java)
        }
        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
        unsetCordappPackages()
    }

    @Test
    fun `deve emitir um novo vencimento`() {
        val para = b.info.legalIdentities.first()
        val valor = 100
        val dataVencimento = Instant.now()

        val flow = EmitirVencimentoFlow.Initiator(
                para,
                valor,
                dataVencimento)

        val future = a.services.startFlow(flow)
        network.runNetwork()
        future.resultFuture.getOrThrow()

        listOf(a, b).forEach {
            it.database.transaction {
                val state = it.services.vaultService.queryBy<VencimentoState>()
                        .states.single().state.data

                val de = a.info.legalIdentities.first()

                assertEquals(de, state.de)
                assertEquals(para, state.para)
                assertEquals(valor, state.valor)
                assertEquals(dataVencimento, state.dataVencimento)
                assertEquals(StatusVencimento.Emitido, state.statusVencimento)
            }
        }

    }

    @Test
    fun `não deve ser possivel emitir um vencimento para si mesmo`() {
        val flow = EmitirVencimentoFlow.Initiator(
                a.info.legalIdentities.first(),
                100,
                Instant.now())
        val future = a.services.startFlow(flow)
        network.runNetwork()
        assertFailsWith<TransactionVerificationException.ContractRejection> {
            future.resultFuture.getOrThrow()
        }
    }

    @Test
    fun `deve aceitar um vencimento`() {
        val para = b.info.legalIdentities.first()
        val valor = 100
        val dataVencimento = Instant.now()

        val flow = EmitirVencimentoFlow.Initiator(
                para,
                valor,
                dataVencimento)

        val future = a.services.startFlow(flow)
        network.runNetwork()

        val tx = future.resultFuture.getOrThrow()

        val aceitarFlow = AceitarVencimentoFlow.Initiator(
                tx.tx.outputsOfType<VencimentoState>().single().linearId.id )

        val aceitarFuture = b.services.startFlow(aceitarFlow)
        network.runNetwork()

        aceitarFuture.resultFuture.getOrThrow()

        listOf(a, b).forEach {
            it.database.transaction {
                val state = it.services.vaultService.queryBy<VencimentoState>()
                        .states.single().state.data

                val de = a.info.legalIdentities.first()

                assertEquals(de, state.de)
                assertEquals(para, state.para)
                assertEquals(valor, state.valor)
                assertEquals(dataVencimento, state.dataVencimento)
                assertEquals(StatusVencimento.APagar, state.statusVencimento)
            }
        }

    }
}