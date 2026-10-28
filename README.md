# CT2003_Restaurant-Rush
 A turn-based restaurant management game with a command-line interface. You are the manager: each turn you choose what the Host, Waiter and Chef do.

#Guide to play the game
 Each turn the screen shows the turn number, revenue, average satisfaction of paid customers, served count, unhappy  departures, the waiting queue, both tables (with order progress, "READY" when cooked) and the staff       available.
 
 Then answer the prompts:
 Host | 0 Wait, 1 Seat the next queued customer (then pick table 1 or 2) 
 Waiter (one task per turn) | 0 Wait, 1 Seat, 2 Take Order, 3 Serve Order (then pick table) 
 Chef (one prep unit per turn) | 0 Wait, 1 Cook Order (then pick table) 
 Happy Hour | y/n, only shown while it is still available (once per game) 

 Menu Pricing, Combo Meals and Customer-Type Discounts
 Regular customer | Pasta | $18.00 | 2 
 VIP | Burger + Salad combo (10% off the $24.00 total = $21.60, then the VIP's 10% off = $19.44) | $19.44 | 2 
 Critic | Salad | $9.00 | 1 
 
 A combo costs 90% of its two items' combined price and needs the sum of their prep units
 (for example Salad + Pasta = $24.30, 3 units).

 Invalid input is re-asked. An invalid move (for example serving a dish that is not READY) is rejected with
 the reason, nothing is used up, and you choose again. After each turn a "WHAT HAPPENED" list shows seating,
 orders (with the dish chosen and price), cooking progress, serving, payments and walk-outs.

 A turn runs in this order: arrivals, manager choices, kitchen and serving, eating and payment, waiting and
 abandonment, advance. The game always runs all 18 turns, then shows Victory or Defeat.

 To Win:at least 4 paid customers AND revenue of at least $45.00 AND average satisfaction of paid
 customers of at least 60.

#Rules
 Arrivals | Turns 1, 4, 7, 10, 13, 16. Turn 7 is a VIP, turn 13 is a Critic, the rest are Regular 
 Satisfaction loss per turn waiting or seated, not yet served | Regular 8, VIP 5, Critic 12. Reaching 0 means the customer leaves, the order is cancelled and the table is freed.
 Serving | A dish can be served only if it was already READY before the Waiter acts. A dish finished this turn is  served next turn at the earliest 
 Payment | Served on turn T, READY_TO_PAY on T+1, paid on T+2. The Cashier pays at most one customer per turn, oldest first 
 VIP bill | 10% off
 Happy Hour | Once per game, lasts the turn it starts and the next. Orders taken while it is active get 20% off(VIP discount first, then Happy Hour). The price is locked when the order is taken. Waiting or seated           customers recover 15 satisfaction (max 100) on each of the two turns 
 Host | Seats at most one queued customer per turn, in addition to the Waiter's task          
 Money | Stored as integer cents, shown as dollars with two decimals 


