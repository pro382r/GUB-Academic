import tkinter as tk
from tkinter import ttk, messagebox, scrolledtext
import hashlib
import time
from datetime import datetime
import threading
from collections import defaultdict

class Block:
    def __init__(self, index, data, previous_hash):
        self.index = index
        self.timestamp = time.time()
        self.data = data
        self.previous_hash = previous_hash
        self.nonce = 0
        self.hash = self.calculate_hash()
    
    def calculate_hash(self):
        return hashlib.sha256(f"{self.index}{self.timestamp}{self.nonce}{self.previous_hash}{self.data}".encode()).hexdigest()
    
    def mine_block(self, difficulty, log_callback):
        target = "0" * difficulty
        log_callback(f"Mining block {self.index}...\n")
        while not self.hash.startswith(target):
            self.nonce += 1
            self.hash = self.calculate_hash()
        log_callback(f"Block {self.index} Mined! Hash: {self.hash}\n")
    
    def __str__(self):
        display_prev_hash = self.previous_hash[:10] + "..." if len(self.previous_hash) > 10 else self.previous_hash
        display_hash = self.hash[:10] + "..." if len(self.hash) > 10 else self.hash
        return (f"Block #{self.index}\n  Timestamp: {datetime.fromtimestamp(self.timestamp)}\n  Data: {self.data}\n"
                f"  Previous Hash: {display_prev_hash}\n  Hash: {display_hash}\n  Nonce: {self.nonce}\n")

class Blockchain:
    def __init__(self, difficulty):
        self.difficulty = difficulty
        self.chain = [Block(0, "Genesis Block", "0")]
    
    def add_block(self, new_block, log_callback):
        new_block.previous_hash = self.chain[-1].hash
        new_block.mine_block(self.difficulty, log_callback)
        self.chain.append(new_block)
    
    def is_chain_valid(self):
        for i in range(1, len(self.chain)):
            current, previous = self.chain[i], self.chain[i-1]
            if current.hash != current.calculate_hash() or previous.hash != current.previous_hash or not current.hash.startswith("0" * self.difficulty):
                return False
        return True

class VotingSystemGUI:
    def __init__(self, root):
        self.root = root
        self.root.title("Blockchain Voting System")
        self.root.geometry("900x720")
        self.blockchain = Blockchain(2)
        self.vote_counts = defaultdict(int)
        self.voted_voters = set()
        self.create_widgets()
        self.update_blockchain_display()
        self.update_result_display()
    
    def create_widgets(self):
        style = ttk.Style()
        style.theme_use('clam')
        style.configure("TFrame", background="#f0f8ff")
        style.configure("TLabel", background="#f0f8ff")
        style.configure("Vote.TButton", background="#ff4500", foreground="white", font=("Arial", 12))
        
        main_frame = ttk.Frame(self.root, padding="10")
        main_frame.pack(fill=tk.BOTH, expand=True)
        
        top_panel = ttk.LabelFrame(main_frame, text="Cast Your Vote", padding="10")
        top_panel.pack(fill=tk.X, padx=5, pady=5)
        
        ttk.Label(top_panel, text="Voter ID:").grid(row=0, column=0, sticky=tk.W, padx=5, pady=5)
        self.voter_id_entry = ttk.Entry(top_panel, width=30)
        self.voter_id_entry.grid(row=0, column=1, padx=5, pady=5)
        
        ttk.Label(top_panel, text="Choose Candidate:").grid(row=1, column=0, sticky=tk.W, padx=5, pady=5)
        self.candidate_var = tk.StringVar()
        candidate_frame = ttk.Frame(top_panel)
        candidate_frame.grid(row=1, column=1, sticky=tk.W)
        for cand in ["A", "B", "C"]:
            ttk.Radiobutton(candidate_frame, text=f"Candidate {cand}", variable=self.candidate_var, value=cand).pack(side=tk.LEFT, padx=5)
        
        self.vote_button = ttk.Button(top_panel, text="Vote", command=self.cast_vote, style="Vote.TButton")
        self.vote_button.grid(row=2, column=1, sticky=tk.E, padx=5, pady=5)
        
        center_panel = ttk.Frame(main_frame)
        center_panel.pack(fill=tk.BOTH, expand=True, padx=5, pady=5)
        
        blockchain_frame = ttk.LabelFrame(center_panel, text="Blockchain Ledger", width=600)
        blockchain_frame.pack(side=tk.LEFT, fill=tk.BOTH, expand=True, padx=5)
        self.blockchain_display = scrolledtext.ScrolledText(blockchain_frame, width=60, height=20, font=("Courier", 12), wrap=tk.WORD)
        self.blockchain_display.pack(fill=tk.BOTH, expand=True)
        
        results_frame = ttk.LabelFrame(center_panel, text="Voting Results", width=300)
        results_frame.pack(side=tk.LEFT, fill=tk.BOTH, padx=5)
        self.results_display = scrolledtext.ScrolledText(results_frame, width=30, height=20, font=("Arial", 12), wrap=tk.WORD)
        self.results_display.pack(fill=tk.BOTH, expand=True)
        
        log_frame = ttk.LabelFrame(main_frame, text="System Log", padding="10")
        log_frame.pack(fill=tk.BOTH, padx=5, pady=5)
        self.log_area = scrolledtext.ScrolledText(log_frame, width=100, height=8, font=("Courier", 9), foreground="blue", wrap=tk.WORD)
        self.log_area.pack(fill=tk.BOTH, expand=True)
    
    def cast_vote(self):
        voter_id = self.voter_id_entry.get().strip()
        candidate = self.candidate_var.get()
        if not voter_id or not candidate:
            messagebox.showerror("Input Error", "Please enter Voter ID and select a candidate.")
            return
        self.vote_button.config(state=tk.DISABLED)
        self.log_message(f"Attempting to cast vote for Voter ID: {voter_id}, Candidate: {candidate}...\n")
        threading.Thread(target=self.process_vote, args=(voter_id, candidate), daemon=True).start()
    
    def process_vote(self, voter_id, candidate):
        if voter_id in self.voted_voters:
            self.root.after(0, lambda: messagebox.showwarning("Vote Error", f"Voter ID {voter_id} has already voted!"))
            self.root.after(0, self.enable_vote_button)
            self.log_message("Vote failed: Voter already voted.\n")
            return
        try:
            vote_data = f"VoterID:{voter_id}, Candidate:{candidate}"
            new_block = Block(len(self.blockchain.chain), vote_data, "")
            self.blockchain.add_block(new_block, self.log_message)
            self.root.after(0, self.update_ui_after_vote, candidate, voter_id)
        except Exception as e:
            self.root.after(0, lambda: messagebox.showerror("Error", f"An error occurred: {str(e)}"))
            self.root.after(0, self.enable_vote_button)
            self.log_message(f"Error during vote: {str(e)}\n")
    
    def update_ui_after_vote(self, candidate, voter_id):
        self.update_blockchain_display()
        self.vote_counts[candidate] += 1
        self.voted_voters.add(voter_id)
        self.update_result_display()
        self.log_message("Vote successful! Block added to chain.\n")
        self.enable_vote_button()
        self.voter_id_entry.delete(0, tk.END)
        self.candidate_var.set("")
    
    def enable_vote_button(self):
        self.vote_button.config(state=tk.NORMAL)
    
    def update_blockchain_display(self):
        self.blockchain_display.config(state=tk.NORMAL)
        self.blockchain_display.delete(1.0, tk.END)
        for block in self.blockchain.chain:
            self.blockchain_display.insert(tk.END, str(block) + "-" * 40 + "\n\n")
        self.blockchain_display.config(state=tk.DISABLED)
        self.blockchain_display.see(tk.END)
    
    def update_result_display(self):
        self.results_display.config(state=tk.NORMAL)
        self.results_display.delete(1.0, tk.END)
        self.results_display.insert(tk.END, "--- Current Results ---\n")
        for candidate, votes in sorted(self.vote_counts.items(), key=lambda x: x[1], reverse=True):
            self.results_display.insert(tk.END, f"Candidate {candidate}: {votes} votes\n")
        self.results_display.insert(tk.END, "\n-----------------------\n")
        status = "Blockchain is VALID! ✅\n" if self.blockchain.is_chain_valid() else "Blockchain is INVALID! 🚨 (Tampering Detected)\n"
        self.results_display.insert(tk.END, status)
        self.results_display.config(state=tk.DISABLED)
    
    def log_message(self, message):
        self.log_area.config(state=tk.NORMAL)
        self.log_area.insert(tk.END, message)
        self.log_area.config(state=tk.DISABLED)
        self.log_area.see(tk.END)

if __name__ == "__main__":
    root = tk.Tk()
    app = VotingSystemGUI(root)
    root.mainloop()