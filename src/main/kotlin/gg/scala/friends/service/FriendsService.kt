package gg.scala.friends.service

import gg.scala.friends.friendship.Friendship
import gg.scala.friends.handler.KFriendHandler
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Service for managing player friendships.
 * This is a compatibility wrapper around KFriendHandler.
 *
 * @author Limo
 * @date 2/5/2026
 */
object FriendsService
{
    fun getAllFriendshipsOf(player: UUID): CompletableFuture<List<Friendship>>
    {
        return KFriendHandler.getAllFriendshipsOf(player)
    }

    fun getFriendshipBetween(playerOne: UUID, playerTwo: UUID): CompletableFuture<Friendship?>
    {
        return KFriendHandler.getFriendshipBetween(playerOne, playerTwo)
    }

    fun areFriends(playerOne: UUID, playerTwo: UUID): CompletableFuture<Boolean>
    {
        return getFriendshipBetween(playerOne, playerTwo).thenApply { it != null }
    }

    fun addFriendship(playerOne: UUID, playerTwo: UUID): CompletableFuture<Void>
    {
        return KFriendHandler.createFriendshipBetween(playerOne, playerTwo)
    }

    fun removeFriendship(playerOne: UUID, playerTwo: UUID): CompletableFuture<Void?>
    {
        return KFriendHandler.deleteFrienshipBetween(playerOne, playerTwo)
    }
}
