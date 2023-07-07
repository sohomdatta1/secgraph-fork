using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Microsoft.Research.WeightedPINQ
{
    public class Task
    {
        public long priority;
        public string taskname;

        public virtual Task[] Work()
        {
            Console.WriteLine("in default Work()"); return null;
        }

        public static void Start(int parallelism, Action<int> actions)
        {
            // performs construction (and replaying contents) concurrently
            var tasks = Enumerable.Range(0, parallelism)
                                  .Select(i => System.Threading.Tasks.Task.Factory.StartNew(() => actions(i)))
                                  .ToArray();

            System.Threading.Tasks.Task.WaitAll(tasks);
        }

        public static void Start(Task[] taskArray)
        {
            Task.Start(taskArray.Length, i => taskArray[i].Start(i));
        }

        public static void Start(int index, List<Task> intasks)
        {
            var tasks = new PriorityList<Task>();
            for (int i = 0; i < intasks.Count; i++)
                tasks.Add(intasks[i], intasks[i].priority);

            while (tasks.list.Count > 0)
            {
                var next = tasks.Min();
                var todo = next.element.Work();

                if (todo != null)
                    for (int i = 0; i < todo.Length; i++)
                        if (todo[i] != null) 
                            tasks.Add(todo[i], todo[i].priority);
            }
        }

        public void Start(int index)
        {
            var tasks = new PriorityList<Task>();

            tasks.Add(this, this.priority);

            while (tasks.list.Count > 0)
            {
                var next = tasks.Min();
                var todo = next.element.Work();

                if (todo != null)
                    for (int i = 0; i < todo.Length; i++)
                        if (todo[i] != null)
                            tasks.Add(todo[i], todo[i].priority);
            }
        }

        static int p = 0;

        public Task(string n)
        {
            taskname = n;
            priority = p++;
        }
    }

    public class PriorityList<T>
    {
        public struct PriorityElement
        {
            public T element;
            public double priority;

            public override string ToString() { return "" + element + "\t" + priority; }

            public PriorityElement(T e, double p) { element = e; priority = p; }
        }

        public ResizeableSubArray<PriorityElement> list;

        public void Add(T element, double priority)
        {
            for (int i = 0; i < list.Count; i++)
                if (list.Array[i].priority == priority)
                    return;

            list.Add(new PriorityElement(element, priority));
        }

        public PriorityElement Min()
        {
            var index = 0;
            for (int i = 1; i < list.Count; i++)
                if (list.Array[i].priority < list.Array[index].priority)
                    index = i;

            var result = list.Array[index];
            list.Array[index] = list.Array[list.Count - 1];
            list.Count--;

            return result;
        }
        public PriorityList()
        {
            list = new ResizeableSubArray<PriorityElement>(0);
        }

    }
}
