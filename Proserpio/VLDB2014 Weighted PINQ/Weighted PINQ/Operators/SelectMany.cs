using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Microsoft.Research.WeightedPINQ.Operators
{
    public class SelectManyChannel<T, R> : SendChannel<Weighted<T>>
        where T : IEquatable<T>
        where R : IEquatable<R>
    {
        Func<T, IEnumerable<R>> selector;
        SendChannel<Weighted<R>> baseChannel;

        Message<Weighted<R>> newMessage = new Message<Weighted<R>>(null);

        public void Send(ref Message<Weighted<T>> message)
        {
            for (int i = 0; i < message.length; i++)
            {
                var results = selector(message.payload[i].record).ToArray();
                for (int j = 0; j < results.Length; j++)
                {
                    if (newMessage.length == newMessage.payload.Length)
                    {
                        newMessage.status = MessageStatus.Normal;
                        baseChannel.Send(ref newMessage);
                        newMessage.length = 0;
                    }

                    newMessage.payload[newMessage.length++] = new Weighted<R>(results[j], message.payload[i].weight / results.Length);
                }
            }

            newMessage.status = message.status;
            baseChannel.Send(ref newMessage);
            newMessage.length = 0;
        }

        public SelectManyChannel(SendChannel<Weighted<R>> channel, Func<T, IEnumerable<R>> s) { baseChannel = channel; selector = s; }
    }

    public class SelectMany<T, R> : UnaryCollectionShard<Weighted<T>, Weighted<R>>
        where T : IEquatable<T>
        where R : IEquatable<R>
    {
        CollectionShard<Weighted<T>> source1;
        Func<T, IEnumerable<R>> selector;

        public override void Subscribe(SendChannel<Weighted<R>> channel, Task task)
        {
            var selectChannel = new SelectManyChannel<T, R>(channel, selector);

            source1.Subscribe(selectChannel, task);
        }

        public SelectMany(CollectionShard<Weighted<T>> s1, Channel<Weighted<T>> channel1, Func<T, IEnumerable<R>> s)
            : base("SelectMany", null, null)
        {
            source1 = s1;
            selector = s;
        }
    }
}
