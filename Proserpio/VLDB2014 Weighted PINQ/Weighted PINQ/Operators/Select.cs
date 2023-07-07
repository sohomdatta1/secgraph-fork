using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Microsoft.Research.WeightedPINQ.Operators
{
    public class SelectChannel<T,R> : SendChannel<Weighted<T>> where T : IEquatable<T> where R : IEquatable<R>
    {
        Func<T, R> selector;
        SendChannel<Weighted<R>> baseChannel;

        Message<Weighted<R>> newMessage = new Message<Weighted<R>>(null);

        public void Send(ref Message<Weighted<T>> message)
        {
            newMessage.status = message.status;
            for (int i = 0; i < message.length; i++)
                newMessage.payload[newMessage.length++] = new Weighted<R>(selector(message.payload[i].record), message.payload[i].weight);

            baseChannel.Send(ref newMessage);

            newMessage.length = 0;
        }

        public SelectChannel(SendChannel<Weighted<R>> channel, Func<T, R> s) { baseChannel = channel; selector = s; }
    }

    public class Select<T,R> : UnaryCollectionShard<Weighted<T>, Weighted<R>> where T : IEquatable<T> where R : IEquatable<R>
    {
        CollectionShard<Weighted<T>> source1;
        Func<T, R> selector;

        public override void Subscribe(SendChannel<Weighted<R>> channel, Task task)
        {
            var selectChannel = new SelectChannel<T,R>(channel, selector);

            source1.Subscribe(selectChannel, task);
        }

        public Select(CollectionShard<Weighted<T>> s1, Channel<Weighted<T>> channel1, Func<T, R> s)
            : base("Select", null, null)
        {
            source1 = s1;
            selector = s;
        }
    }
}
