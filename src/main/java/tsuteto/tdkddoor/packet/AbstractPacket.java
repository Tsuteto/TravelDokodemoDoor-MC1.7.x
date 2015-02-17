package tsuteto.tdkddoor.packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;


/**
 * AbstractPacket クラス PacketPipelineで扱うパケットはすべてこのクラスを継承する.
 * @author sirgingalot
 */
public abstract class AbstractPacket {

    /**
     * パケットのデータをByteBufに変換する. 複雑なデータ（NBTとか）はハンドラーが用意されている ( @link{cpw.mods.fml.common.network.ByteBuffUtils}を見てくれ)
     *
     * @param ctx    channel context（和訳注：殆ど使わない）
     * @param buffer 変換するByteBufの変数。これにwriteしていく。
     */
    public abstract void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer);

    /**
     * ByteBufからデータを取り出す. 複雑なデータはハンドラーが用意されている ( @link{cpw.mods.fml.common.network.ByteBuffUtils}を見てくれ)
     *
     * @param ctx    channel context（普段は使わない）
     * @param buffer データを取り出すByteBuf。これからreadしていく。
     */
    public abstract void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer);

    /**
     * クライアント側でパケットを受け取った後処理するメソッド。decodeIntoでByteBufが読み出されたあとに実行される。
     *
     * @param player the player reference
     */
    public abstract void handleClientSide(EntityPlayer player);

    /**
     * サーバー側でパケットを受け取った後処理するメソッド。decodeIntoでByteBufが読み出されたあとに実行される。
     *
     * @param player the player reference
     */
    public abstract void handleServerSide(EntityPlayer player);
}